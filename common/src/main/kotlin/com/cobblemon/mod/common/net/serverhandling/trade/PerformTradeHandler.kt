/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.trade

import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.net.messages.client.trade.TradeCancelledPacket
import com.cobblemon.mod.common.net.messages.server.trade.PerformTradePacket
import com.cobblemon.mod.common.trade.TradeManager
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object PerformTradeHandler : ServerNetworkPacketHandler<PerformTradePacket> {
    override fun handle(packet: PerformTradePacket, server: MinecraftServer, player: ServerPlayer) {
        val trade = TradeManager.getActiveTrade(player.uuid) ?: return player.sendPacket(TradeCancelledPacket())
        val tradeParticipant = trade.getTradeParticipant(player.uuid)
        if (trade.getOpposingOffer(tradeParticipant).pokemon?.uuid == packet.pokemonOfferId) {
            trade.performTrade(tradeParticipant)
        }
    }
}