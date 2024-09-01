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
import com.cobblemon.mod.common.net.messages.client.trade.TradeUpdatedPacket
import net.minecraft.client.Minecraft

object TradeUpdatedHandler : ClientNetworkPacketHandler<TradeUpdatedPacket> {
    override fun handle(packet: TradeUpdatedPacket, client: Minecraft) {
        val trade = CobblemonClient.trade ?: return

        if (packet.playerId == Minecraft.getInstance().player?.uuid) {
            trade.myOffer.set(packet.pokemon)
        } else {
            trade.oppositeOffer.set(packet.pokemon)
        }
        trade.oppositeAcceptedMyOffer.set(false)
        trade.acceptedOppositeOffer = false
    }
}