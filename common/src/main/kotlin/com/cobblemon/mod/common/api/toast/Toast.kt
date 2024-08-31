/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.toast

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.scheduling.afterOnServer
import com.cobblemon.mod.common.api.toast.Toast.Companion.VANILLA_FRAME
import com.cobblemon.mod.common.api.toast.Toast.Companion.VANILLA_PROGRESS_COLOR
import com.cobblemon.mod.common.net.messages.client.toast.ToastPacket
import com.cobblemon.mod.common.platform.events.PlatformEvents
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.world.item.ItemStack
import java.util.*
import kotlin.properties.Delegates

/**
 * Represents a toast that can be shown to multiple clients.
 * This implementation will automatically synchronize changes to the object to any online listener at the time of the update.
 * Any offline listener will be automatically synchronized once they log in again.
 *
 * @param title The [Component] representing the toast title, this can be a [Component.empty].
 * @param description The [Component] representing the toast description, this can be a [Component.empty].
 * @param icon The [ItemStack] that is rendered as the toast icon, this can be a [ItemStack.EMPTY].
 * @param frameTexture The texture of the frame, default is the [VANILLA_FRAME].
 * @param progress The value of the progress bar, this accepts a value between 0.0F and 1.0F, any other value will not render a progress bar.
 * @param progressColor The progress bar color in RGB, default is the [VANILLA_PROGRESS_COLOR].
 *
 * @since June 2nd, 2023
 */
class Toast(
    title: Component,
    description: Component,
    icon: ItemStack,
    frameTexture: ResourceLocation = VANILLA_FRAME,
    progress: Float = -1F,
    progressColor: Int = VANILLA_PROGRESS_COLOR
) {

    /**
     * The [Component] representing the toast title, this can be a [Component.empty].
     */
    var title: Component by Delegates.observable(title) { _, old, new -> if (old != new) this.launchUpdate() }

    /**
     * The [Component] representing the toast description, this can be a [Component.empty].
     */
    var description: Component by Delegates.observable(description) { _, old, new -> if (old != new) this.launchUpdate() }

    /**
     * The [ItemStack] that is rendered as the toast icon, this can be a [ItemStack.EMPTY].
     */
    var icon: ItemStack by Delegates.observable(icon) { _, old, new -> if (old != new) this.launchUpdate() }

    /**
     * The texture of the frame.
     */
    var frameTexture: ResourceLocation by Delegates.observable(frameTexture) { _, old, new -> if (old != new) this.launchUpdate() }

    /**
     * The value of the progress bar, this accepts a value between 0.0F and 1.0F, any other value will not render a progress bar.
     */
    var progress: Float by Delegates.observable(progress) { _, old, new -> if (old != new) this.launchUpdate() }

    /**
     * The progress bar color in RGB.
     */
    var progressColor: Int by Delegates.observable(progressColor) { _, old, new -> if (old != new) this.launchUpdate() }

    /**
     * The online listeners mapped to the [ServerPlayer].
     */
    val listeners: Collection<ServerPlayer> get() = this.listenerUuids.mapNotNull { Cobblemon.implementation.server()?.playerList?.getPlayer(it) }

    /**
     * Used internally to track this toast on the client.
     */
    internal val uuid: UUID = Mth.createInsecureUUID()

    /**
     * The listeners to this toast.
     * This contains all of them regardless of online status for online mapped to [ServerPlayer] use [listeners].
     */
    private val listenerUuids = hashSetOf<UUID>()

    /**
     * The subscription to the [PlatformEvents.SERVER_PLAYER_LOGIN].
     * This is automatically unsubscribed when [expire] is invoked.
     */
    private val subscription = PlatformEvents.SERVER_PLAYER_LOGIN.subscribe { event ->
        if (this.listenerUuids.contains(event.player.uuid)) {
            this.updateFor(event.player, this.toPacket(ToastPacket.Behaviour.SHOW_OR_UPDATE))
        }
    }

    /**
     * Add listeners to this toast.
     * They will be automatically synced with the current object status.
     *
     * @param listeners The [ServerPlayer] being added.
     */
    fun addListeners(vararg listeners: ServerPlayer) {
        val packet = this.toPacket(ToastPacket.Behaviour.SHOW_OR_UPDATE)
        listeners.forEach { listener ->
            if (this.listenerUuids.add(listener.uuid)) {
                this.updateFor(listener, packet)
            }
        }
    }

    /**
     * Removes listeners from this toast.
     * They will have this toast automatically hidden.
     * Please note this will not consider the lifecycle of the toast as ended.
     * To properly do that use [expire].
     *
     * @param listeners The [ServerPlayer] being removed.
     */
    fun removeListeners(vararg listeners: ServerPlayer) {
        val packet = this.toPacket(ToastPacket.Behaviour.HIDE)
        listeners.forEach { listener ->
            if (this.listenerUuids.remove(listener.uuid)) {
                this.updateFor(listener, packet)
            }
        }
    }

    /**
     * Invokes [expire] after the specified [ticks].
     *
     * @param ticks The number of ticks before the lifecycle of this toast ends.
     */
    fun expireAfter(ticks: Int) {
        afterOnServer(ticks = ticks) {
            this.expire()
        }
    }

    /**
     * Utility method to hide the progress bar.
     * See [progress] for the details on the possible values.
     */
    fun setNoProgress() {
        this.progress = -1F
    }

    /**
     * Ends the lifecycle of this toast.
     * It will automatically hide the toast for all listeners and stop automatic sync.
     */
    fun expire() {
        this.removeListeners(*this.listeners.toTypedArray())
        this.listenerUuids.clear()
        this.subscription.unsubscribe()
    }

    private fun launchUpdate() {
        val packet = this.toPacket(ToastPacket.Behaviour.SHOW_OR_UPDATE)
        this.listeners.forEach { player -> Cobblemon.implementation.networkManager.sendPacketToPlayer(player, packet) }
    }

    /**
     * Updates the given player with the packet state.
     *
     * @param player The [ServerPlayer] being updated.
     * @param packet The outbound [ToastPacket].
     */
    private fun updateFor(player: ServerPlayer, packet: ToastPacket) {
        Cobblemon.implementation.networkManager.sendPacketToPlayer(player, packet)
    }

    /**
     * Maps this instance to the [ToastPacket].
     *
     * @param behaviour The [ToastPacket.Behaviour] of this update.
     * @return The generated [ToastPacket].
     */
    private fun toPacket(behaviour: ToastPacket.Behaviour): ToastPacket = ToastPacket(
        this.title,
        this.description,
        this.icon,
        this.frameTexture,
        this.progress,
        this.progressColor,
        this.uuid,
        behaviour
    )

    companion object {

        /**
         * The default Minecraft toast frame texture.
         */
        val VANILLA_FRAME = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/toasts.png")

        const val VANILLA_PROGRESS_COLOR = -1675545

    }

}