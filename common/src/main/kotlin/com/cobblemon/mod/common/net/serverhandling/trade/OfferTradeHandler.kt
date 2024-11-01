/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.trade

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.trade.TradeManager.TradeRequest
import com.cobblemon.mod.common.net.messages.client.trade.TradeOfferNotificationPacket
import com.cobblemon.mod.common.net.messages.server.trade.OfferTradePacket
import com.cobblemon.mod.common.trade.TradeManager
import com.cobblemon.mod.common.util.getPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

/**
 * Processes a player's interaction request to trade with another player. If valid, creates a respective [TradeRequest]
 * and sends a [TradeOfferNotificationPacket] to the player to decide upon.
 *
 * @author Hiroku
 * @since March 12th, 2023
 */
object OfferTradeHandler : ServerNetworkPacketHandler<OfferTradePacket> {
    override fun handle(packet: OfferTradePacket, server: MinecraftServer, player: ServerPlayer) {
        val targetPlayerEntity = packet.offeredPlayerId.getPlayer() ?: return
        TradeManager.sendRequest(TradeRequest(player, targetPlayerEntity))
    }
}