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
import com.cobblemon.mod.common.net.messages.server.trade.CancelTradePacket
import com.cobblemon.mod.common.net.messages.server.trade.UpdateTradeOfferPacket
import com.cobblemon.mod.common.trade.TradeManager
import com.cobblemon.mod.common.util.party
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object UpdateTradeOfferHandler : ServerNetworkPacketHandler<UpdateTradeOfferPacket> {
    override fun handle(packet: UpdateTradeOfferPacket, server: MinecraftServer, player: ServerPlayer) {
        val trade = TradeManager.getActiveTrade(player.uuid) ?: return player.sendPacket(CancelTradePacket())
        val tradeParticipant = trade.getTradeParticipant(player.uuid)
        val newOffer = packet.newOffer
        if (newOffer == null) {
            trade.updateOffer(tradeParticipant, null)
        } else {
            val (pokemonId, partyPosition) = newOffer
            val party = player.party()
            val pokemon = party[partyPosition]
            if (pokemon == null || pokemon.uuid != pokemonId) {
                return
            } else if (!pokemon.tradeable) {
                return
            } else {
                trade.updateOffer(tradeParticipant, pokemon)
            }
        }
    }
}