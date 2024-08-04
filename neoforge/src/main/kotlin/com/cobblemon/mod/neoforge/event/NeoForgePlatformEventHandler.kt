/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.neoforge.event

import com.cobblemon.mod.common.platform.events.ChangeDimensionEvent
import com.cobblemon.mod.common.platform.events.PlatformEvents
import com.cobblemon.mod.common.platform.events.ServerEvent
import com.cobblemon.mod.common.platform.events.ServerPlayerEvent
import com.cobblemon.mod.common.platform.events.ServerTickEvent
import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent
import net.neoforged.neoforge.event.server.ServerStartedEvent
import net.neoforged.neoforge.event.server.ServerStoppedEvent
import net.neoforged.neoforge.event.server.ServerStoppingEvent

object NeoForgePlatformEventHandler {

    fun register() {
        NeoForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun serverStarting(e: ServerAboutToStartEvent) {
        PlatformEvents.SERVER_STARTING.post(ServerEvent.Starting(e.server))
    }

    @SubscribeEvent
    fun serverStarted(e: ServerStartedEvent) {
        PlatformEvents.SERVER_STARTED.post(ServerEvent.Started(e.server))
    }


    @SubscribeEvent
    fun serverStopping(e: ServerStoppingEvent) {
        PlatformEvents.SERVER_STOPPING.post(ServerEvent.Stopping(e.server))
    }

    @SubscribeEvent
    fun serverStopped(e: ServerStoppedEvent) {
        PlatformEvents.SERVER_STOPPED.post(ServerEvent.Stopped(e.server))
    }

    @SubscribeEvent
    fun preServerTick(e: net.neoforged.neoforge.event.tick.ServerTickEvent.Pre) {
        PlatformEvents.SERVER_TICK_PRE.post(ServerTickEvent.Pre(e.server))
    }

    @SubscribeEvent
    fun postServerTick(e: net.neoforged.neoforge.event.tick.ServerTickEvent.Post) {
        PlatformEvents.SERVER_TICK_POST.post(ServerTickEvent.Post(e.server))
    }

    @SubscribeEvent
    fun onLogin(e: PlayerEvent.PlayerLoggedInEvent) {
        val player = e.entity as? ServerPlayer ?: return
        PlatformEvents.SERVER_PLAYER_LOGIN.post(ServerPlayerEvent.Login(player))
    }

    @SubscribeEvent
    fun onLogout(e: PlayerEvent.PlayerLoggedOutEvent) {
        val player = e.entity as? ServerPlayer ?: return
        PlatformEvents.SERVER_PLAYER_LOGOUT.post(ServerPlayerEvent.Logout(player))
    }

    @SubscribeEvent
    fun onDeath(e: LivingDeathEvent) {
        val player = e.entity as? ServerPlayer ?: return
        PlatformEvents.PLAYER_DEATH.postThen(
            event = ServerPlayerEvent.Death(player),
            ifSucceeded = {},
            ifCanceled = { e.isCanceled = true }
        )
    }

    @SubscribeEvent
    fun onRightClickBlock(e: PlayerInteractEvent.RightClickBlock) {
        val player = e.entity as? ServerPlayer ?: return
        val hand = e.hand
        val pos = e.pos
        val face = e.face
        PlatformEvents.RIGHT_CLICK_BLOCK.postThen(
            event = com.cobblemon.mod.common.platform.events.ServerPlayerEvent.RightClickBlock(player, pos, hand, face),
            ifSucceeded = {},
            ifCanceled = { e.isCanceled = true }
        )
    }

    @SubscribeEvent
    fun onRightClickEntity(e: PlayerInteractEvent.EntityInteract) {
        val player = e.entity as? ServerPlayer ?: return
        val hand = e.hand
        val item = player.getItemInHand(hand)
        val entity = e.target
        PlatformEvents.RIGHT_CLICK_ENTITY.postThen(
            event = ServerPlayerEvent.RightClickEntity(player, item, hand, entity),
            ifSucceeded = {},
            ifCanceled = { e.isCanceled = true }
        )
    }

    @SubscribeEvent
    fun onChangeDimension(e: PlayerEvent.PlayerChangedDimensionEvent) {
        val player = e.entity
        if (player is ServerPlayer) {
            PlatformEvents.CHANGE_DIMENSION.post(ChangeDimensionEvent(player))
        }
    }
}