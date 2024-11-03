package com.cobblemon.mod.common.api.interaction

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.scheduling.afterOnServer
import com.cobblemon.mod.common.api.text.aqua
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.battles.TeamManager
import com.cobblemon.mod.common.util.*
import net.minecraft.server.level.ServerPlayer
import java.util.*

/**
 * Defines a manager for organizing active interaction requests between parties. Responsible for dispatching requests to clients,
 * notifying senders and receivers of requests, and handling their acceptance, expiration, or cancellation.
 *
 * @param langKey The subkey used for the notification messages sent with for requests.
 *
 * @author Segfault Guy
 * @since October 26th, 2024
 */
abstract class RequestManager<T : ServerPlayerActionRequest> {

    /**
     * Tracking of the active [ServerPlayerActionRequest]s by their sender.
     *
     * NOTE: only 1 request can be sent by a sender at a time.
     */
    private val outboundRequests = mutableMapOf<UUID, T>()

    /**
     * Tracking of the active [ServerPlayerActionRequest]s by their recipient.
     *
     * NOTE: a recipient can receive multiple requests from different senders at the same time.
     */
    private val inboundRequests = mutableMapOf<UUID, MutableList<T>>()

    // we use the term 'sender' because BattleChallenges can be sent TEAM-to-TEAM instead of PLAYER-to-PLAYER for multi battles

    /** Queries the latest pending request that was sent by [senderID]. */
    fun getOutboundRequest(senderID: UUID) = outboundRequests.get(senderID)

    /** Queries the pending request of [requestID] that was sent by [senderID]. */
    fun getOutboundRequest(senderID: UUID, requestID: UUID) = this.getOutboundRequest(senderID)?.takeIf { it.requestID == requestID }

    /** Queries the pending request that was sent by [senderID] and received by [receiverID]. */
    fun getOutboundRequestByRecipient(senderID: UUID, receiverID: UUID) = this.getOutboundRequest(senderID)?.takeIf { it.receiverID == receiverID }

    /** Queries the pending request of [requestID] that was sent by [sender] or their team. */
    fun getOutboundRequest(sender: ServerPlayer, requestID: UUID) = TeamManager.getTeam(sender)?.let { this.getOutboundRequest(it.teamID, requestID) }
        ?: this.getOutboundRequest(sender.uuid, requestID)

    /** Queries all pending requests that were received by [receiverID]. */
    fun getInboundRequests(receiverID: UUID) = inboundRequests.get(receiverID)

    /** Queries the pending request of [requestID] that was received by [receiverID]. */
    fun getInboundRequest(receiverID: UUID, requestID: UUID) = this.getInboundRequests(receiverID)?.find { it.requestID == requestID }

    /** Queries the pending request that was sent by [senderID] and received by [receiverID]. */
    fun getInboundRequestBySender(receiverID: UUID, senderID: UUID) = this.getInboundRequests(receiverID)?.find { it.senderID == senderID }

    /** Queries the pending request of [requestID] that was received by [receiver] or their team. */
    fun getInboundRequest(receiver: ServerPlayer, requestID: UUID) = TeamManager.getTeam(receiver)?.let { this.getInboundRequest(it.teamID, requestID) }
        ?: this.getInboundRequest(receiver.uuid, requestID)

    /** Determines whether [player] can receive a [T]. */
    open fun isBusy(player: ServerPlayer): Boolean = player.isInBattle() || player.isTrading()

    /** Determines if the [target] is a valid interaction request target. */
    abstract fun isValidInteraction(player: ServerPlayer, target: ServerPlayer): Boolean

    /** Determines whether [request] response is valid and can be accepted. If invalid, notifies the sender and receiver of the request why. */
    protected abstract fun canAccept(request: T): Boolean

    /** Creates a packet for notifying a recipient about a received request. */
    protected abstract fun notificationPacket(request: T): NetworkPacket<*>

    /** Creates a packet for canceling received requests. */
    protected abstract fun expirationPacket(request: T): NetworkPacket<*>

    protected fun addRequest(request: T) {
        outboundRequests.put(request.senderID, request)
        inboundRequests.computeIfAbsent(request.receiverID) { mutableListOf() }.add(request)
        request.sendToReceiver(this.notificationPacket(request))
    }

    protected fun removeRequest(request: T): Boolean {
        val inbound = inboundRequests.get(request.receiverID)
        val removeOutbound = outboundRequests.remove(request.senderID, request)
        val removeInbound = inbound?.remove(request) == true
        if (removeInbound && inbound?.size == 0) inboundRequests.remove(request.receiverID, inbound)
        if (removeInbound != removeOutbound)
            Cobblemon.LOGGER.error("RequestManager: PlayerActionRequest desync")  // USE ADDREQUEST AND REMOVEREQUEST

        val isRemoved = removeInbound && removeOutbound
        if (isRemoved) request.sendToReceiver(this.expirationPacket(request))
        return isRemoved
    }

    /** Notifies and removes an outbound request [T] sent by [ServerPlayerActionRequest.sender] to [ServerPlayerActionRequest.receiver]. */
    open fun cancelRequest(request: T, terminator: ServerPlayer? = null) {
        if (!this.removeRequest(request)) return
        // canceled by expiration
        if (terminator == null) {
            request.notifySender(true, "expired.sender", request.receiver.name.copy().aqua())
            request.notifyReceiver(true, "expired.receiver", request.sender.name.copy().aqua())
        }
        // canceled by sender
        else if (request.sender == terminator) {
            request.notifySender(true, "canceled.sender", request.receiver.name.copy().aqua())
            request.notifyReceiver(true, "canceled.receiver", request.sender.name.copy().aqua())
        }
        // canceled by other (RECEIVERS DECLINE, NOT CANCEL. THIS IS FOR OTHER TEAM MEMBERS.)
        else {
            request.notifySender(true, "canceled.other", terminator.name.copy().aqua())
            request.notifyReceiver(true, "canceled.other", terminator.name.copy().aqua())
        }
    }

    /** Sends an outbound request [T] from [ServerPlayerActionRequest.sender] to [ServerPlayerActionRequest.receiver]. */
    open fun sendRequest(request: T): Boolean {
        val existingRequest = this.getOutboundRequest(request.senderID)
        val pendingRequest = this.getInboundRequestBySender(request.senderID, request.receiverID)

        // old request
        if (existingRequest != null && existingRequest.receiverID != request.receiverID)
            this.cancelRequest(existingRequest, existingRequest.sender)
        // if sender already has a request awaiting a response from the receiver they're trying to send to.
        if (existingRequest != null && existingRequest.receiverID == request.receiverID)
            request.notify(request.sender, true, "error.duplicate", request.receiver.name.copy().aqua())
        // if sender already has a pending request from the receiver they're trying to send to.
        else if (pendingRequest != null)
            request.notify(request.sender, true, "error.pending", request.receiver.name.copy().aqua())
        // verify sending player can interact with target player
        else if (!this.isValidInteraction(request.sender, request.receiver))
            request.notify(request.sender, true, "ui.interact.failed")
        else if (this.isBusy(request.receiver))                                 // TODO call to isBusy when setting up the interaction packet
            request.notify(request.sender, true, "ui.interact.unavailable")
        // new request
        else {
            this.addRequest(request)
            afterOnServer(seconds = request.expiryTime.toFloat()) { this.cancelRequest(request) }
            request.notifySender(false, "sent", request.receiver.name.copy().aqua())
            request.notifyReceiver(false, "received", request.sender.name.copy().aqua())
            return true
        }
        return false
    }

    /** Hooks into [acceptRequest] immediately after the [request] is accepted and removed from the manager. */
    protected open fun onAccept(request: T) {}

    /** Accepts an inbound [requestID] for [receiver]. */
    open fun acceptRequest(receiver: ServerPlayer, requestID: UUID): Boolean {
        var accepted = false
        val request = this.getInboundRequest(receiver, requestID)

        // verify request being responded to still valid
        if (request == null)
            receiver.sendSystemMessage(lang("error.request_already_expired").red(), false)
        // verify receiving player can respond to sending player
        else if (!this.isValidInteraction(request.receiver, request.sender))
            request.notify(receiver, true, "ui.interact.failed")
        // if sending player is occupied, can't accept response
        else if (this.isBusy(request.sender))                                   // TODO enhancement: allow retries - modify client so we don't remove request when sending acceptance
            request.notify(receiver, true, "ui.interact.unavailable")
        // if no condition is blocking acceptance, accept
        else if (this.canAccept(request)) {
            request.notifySender(false, "accept.sender",  request.receiver.name.copy().aqua())
            request.notifyReceiver(false, "accept.receiver", request.sender.name.copy().aqua())
            accepted = true
        }

        request?.let {
            this.removeRequest(it)
            if (accepted) this.onAccept(request)    // order matters
        }
        return accepted
    }

    /** Declines an inbound request [requestID] for [receiver]. */
    open fun declineRequest(receiver: ServerPlayer, requestID: UUID) {
        val request = inboundRequests.get(receiver.uuid)?.find { it.requestID == requestID } ?: return
        request.notifySender(false, "decline.sender", request.receiver.name.copy().aqua())
        request.notifyReceiver(false, "decline.receiver", request.sender.name.copy().aqua())
        this.removeRequest(request)
    }

    /** Cancels pending outbound requests sent from, and pending inbound request sent to, [player]. */
    protected open fun onLogoff(player: ServerPlayer) {
        // ONLY regarding the player. see TeamManager for how team requests are canceled on team disband.
        outboundRequests.get(player.uuid)?.let { request ->
            this.cancelRequest(request, player)
        }
        inboundRequests.get(player.uuid)?.let { requests ->
            requests.forEach { request ->
                this.declineRequest(player, request.requestID)
            }
        }
    }

    companion object {
        private val managers = mutableListOf<RequestManager<*>>()

        fun register(manager: RequestManager<*>) = managers.add(manager)

        fun onLogoff(player: ServerPlayer) = managers.forEach { it.onLogoff(player) }
    }
}