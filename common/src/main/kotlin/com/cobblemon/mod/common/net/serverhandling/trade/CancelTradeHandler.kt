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
import com.cobblemon.mod.common.net.messages.server.trade.CancelTradePacket
import com.cobblemon.mod.common.trade.TradeManager
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object CancelTradeHandler : ServerNetworkPacketHandler<CancelTradePacket> {
    override fun handle(packet: CancelTradePacket, server: MinecraftServer, player: ServerPlayer) {
        val trade = TradeManager.getActiveTrade(player.uuid) ?: return player.sendPacket(TradeCancelledPacket())
        trade.cancelTrade()
    }
}