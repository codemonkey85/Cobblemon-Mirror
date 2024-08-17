/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.storage.pc

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.api.storage.pc.link.PCLinkManager
import com.cobblemon.mod.common.net.messages.client.storage.pc.ClosePCPacket
import com.cobblemon.mod.common.net.messages.server.storage.pc.MovePartyPokemonToPCPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object MovePartyPokemonToPCHandler : ServerNetworkPacketHandler<MovePartyPokemonToPCPacket> {
    override fun handle(packet: MovePartyPokemonToPCPacket, server: MinecraftServer, player: ServerPlayer) {
        val party = Cobblemon.storage.getParty(player)
        val pc = PCLinkManager.getPC(player) ?: return run { ClosePCPacket(null).sendToPlayer(player) }
        val pokemon = party[packet.partyPosition] ?: return
        if (pokemon.uuid != packet.pokemonID) {
            return
        }
        if (party.filterNotNull().size == 1 && Cobblemon.config.preventCompletePartyDeposit) {
            return
        }
        val pcPosition = packet.pcPosition?.takeIf { pc[it] == null } ?: pc.getFirstAvailablePosition() ?: return
        party.remove(packet.partyPosition)
        pc[pcPosition] = pokemon
    }
}