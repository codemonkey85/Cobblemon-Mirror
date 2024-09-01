/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.trade

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.net.messages.client.trade.TradeCompletedPacket
import net.minecraft.client.Minecraft

object TradeCompletedHandler : ClientNetworkPacketHandler<TradeCompletedPacket> {
    override fun handle(packet: TradeCompletedPacket, client: Minecraft) {
        val trade = CobblemonClient.trade ?: return
        trade.completedEmitter.emit(packet.pokemonId1 to packet.pokemonId2)
    }
}