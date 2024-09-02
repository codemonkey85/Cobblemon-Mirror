/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.neoforge.client

import com.cobblemon.mod.common.platform.events.ClientPlayerEvent
import com.cobblemon.mod.common.platform.events.ClientTickEvent
import com.cobblemon.mod.common.platform.events.PlatformEvents
import net.minecraft.client.Minecraft
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent


@OnlyIn(Dist.CLIENT)
object NeoForgeClientPlatformEventHandler {

    fun register() {
        NeoForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun preClientTick(e: net.neoforged.neoforge.client.event.ClientTickEvent.Pre) {
        PlatformEvents.CLIENT_TICK_PRE.post(ClientTickEvent.Pre(Minecraft.getInstance()))
    }

    @SubscribeEvent
    fun postClientTick(e: net.neoforged.neoforge.client.event.ClientTickEvent.Post) {
        PlatformEvents.CLIENT_TICK_POST.post(ClientTickEvent.Post(Minecraft.getInstance()))
    }

    @SubscribeEvent
    fun onLogin(e: ClientPlayerNetworkEvent.LoggingIn) {
        PlatformEvents.CLIENT_PLAYER_LOGIN.post(ClientPlayerEvent.Login(e.player))
    }

    @SubscribeEvent
    fun onLogout(e: ClientPlayerNetworkEvent.LoggingOut) {
        PlatformEvents.CLIENT_PLAYER_LOGOUT.post(ClientPlayerEvent.Logout(e.player ?: return))
    }

    @SubscribeEvent
    fun onItemTooltip(e: ItemTooltipEvent) {
        PlatformEvents.CLIENT_ITEM_TOOLTIP.post(com.cobblemon.mod.common.platform.events.ItemTooltipEvent(e.itemStack, e.context, e.flags, e.toolTip))
    }

}