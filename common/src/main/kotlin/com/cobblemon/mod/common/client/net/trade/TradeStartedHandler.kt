/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.trade

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.gui.trade.TradeGUI
import com.cobblemon.mod.common.client.render.ClientPlayerIcon
import com.cobblemon.mod.common.client.trade.ClientTrade
import com.cobblemon.mod.common.net.messages.client.trade.TradeStartedPacket
import com.cobblemon.mod.common.net.messages.client.trade.TradeStartedPacket.TradeablePokemon
import net.minecraft.client.Minecraft

object TradeStartedHandler : ClientNetworkPacketHandler<TradeStartedPacket> {
    override fun handle(packet: TradeStartedPacket, client: Minecraft) {
        Cobblemon.LOGGER.error("handle trade start from: " + packet.traderId)
        val trade = ClientTrade()
        CobblemonClient.trade = trade
        Minecraft.getInstance().setScreen(
            TradeGUI(
                trade,
                packet.traderId,
                packet.traderName,
                packet.traderParty.toMutableList(),
                CobblemonClient.storage.myParty.map { it?.let(::TradeablePokemon) }.toMutableList()
            )
        )
        CobblemonClient.requests.tradeOffers.remove(packet.traderId)
        ClientPlayerIcon.update(packet.traderId)
    }
}