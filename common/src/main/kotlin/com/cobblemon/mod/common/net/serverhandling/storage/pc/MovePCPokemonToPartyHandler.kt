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
import com.cobblemon.mod.common.net.messages.server.storage.pc.MovePCPokemonToPartyPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object MovePCPokemonToPartyHandler : ServerNetworkPacketHandler<MovePCPokemonToPartyPacket> {
    override fun handle(packet: MovePCPokemonToPartyPacket, server: MinecraftServer, player: ServerPlayer) {
        val party = Cobblemon.storage.getParty(player)
        val pc = PCLinkManager.getPC(player) ?: return run { ClosePCPacket(null).sendToPlayer(player) }
        val pokemon = pc[packet.pcPosition] ?: return
        if (pokemon.uuid != packet.pokemonID) {
            return
        }
        val partyPosition = packet.partyPosition?.takeIf { party[it] == null } ?: party.getFirstAvailablePosition() ?: return
        pc.remove(packet.pcPosition)
        party[partyPosition] = pokemon
    }
}