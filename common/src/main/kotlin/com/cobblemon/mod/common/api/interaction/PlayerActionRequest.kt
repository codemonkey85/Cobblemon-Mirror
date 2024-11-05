/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.interaction

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.battles.TeamManager
import com.cobblemon.mod.common.util.lang
import net.minecraft.server.level.ServerPlayer
import java.util.UUID

/**
 * Represents an interaction request between players.
 *
 * @author Segfault Guy
 * @since September 3rd, 2024
 */
interface PlayerActionRequest {
    /** The unique ID of this request. */
    val requestID: UUID

    /** The unique ID of the party that's initiating this request. */
    val senderID: UUID

    /** The amount of seconds this request is valid for. */
    val expiryTime: Int
}

/**
 * An outbound [PlayerActionRequest].
 *
 * @author Segfault Guy
 * @since September 29th, 2024
 */
interface ServerPlayerActionRequest : PlayerActionRequest {

    /** The subkey identifying lang entries associated with this request. */
    val key: String

    /** The player initiating this interaction request. */
    val sender: ServerPlayer

    /** The player that's the target of this interaction request. */
    val receiver: ServerPlayer

    /** The unique ID of the party that's the target of this request. */
    val receiverID: UUID get() = receiver.uuid

    override val senderID: UUID get() = sender.uuid

    /** Sends [packet] to sending party of this request. */
    fun sendToSender(packet: NetworkPacket<*>) = sender.sendPacket(packet)

    /** Sends [packet] to receiving party of this request. */
    fun sendToReceiver(packet: NetworkPacket<*>) = receiver.sendPacket(packet)

    /** Notifies the sending party of this request about [langKey]. */
    fun notifySender(error: Boolean, langKey: String, vararg params: Any) = notify(sender, error, "$key.$langKey", *params)

    /** Notifies the receiving party of this request about [langKey]. */
    fun notifyReceiver(error: Boolean, langKey: String, vararg params: Any) = notify(receiver, error, "$key.$langKey", *params)

    /** System message to inform individual [player] about [langKey]. */
    fun notify(player: ServerPlayer, error: Boolean, langKey: String, vararg params: Any) {
        val lang = lang(langKey, *params).apply { if (error) red() else yellow() }
        player.sendSystemMessage(lang, false)
    }
}

/**
 * An outbound [PlayerActionRequest] made on behalf of a team.
 *
 * @author Segfault Guy
 * @since October 29th, 2024
 */
interface ServerTeamActionRequest : ServerPlayerActionRequest {

    /** The team initiating this request. */
    val senderTeam: TeamManager.MultiBattleTeam

    /** The team that's the target of this request. */
    val receiverTeam: TeamManager.MultiBattleTeam

    override val senderID: UUID get() = senderTeam.teamID

    override val receiverID: UUID get() = receiverTeam.teamID

    override fun sendToSender(packet: NetworkPacket<*>) = CobblemonNetwork.sendPacketToPlayers(senderTeam.teamPlayers, packet)

    override fun sendToReceiver(packet: NetworkPacket<*>) = CobblemonNetwork.sendPacketToPlayers(receiverTeam.teamPlayers, packet)

    override fun notifySender(error: Boolean, langKey: String, vararg params: Any) =
        senderTeam.teamPlayers.forEach { this.notify(it, error, "$key.$langKey", *params) }

    override fun notifyReceiver(error: Boolean, langKey: String, vararg params: Any) =
        receiverTeam.teamPlayers.forEach { this.notify(it, error, "$key.$langKey", *params) }
}