/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.storage.party

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.net.messages.server.storage.party.MovePartyPokemonPacket
import com.cobblemon.mod.common.util.party
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object MovePartyPokemonHandler : ServerNetworkPacketHandler<MovePartyPokemonPacket> {
    override fun handle(packet: MovePartyPokemonPacket, server: MinecraftServer, player: ServerPlayer) {
        val party = player.party()
        val pokemon = party[packet.oldPosition] ?: return
        if (pokemon.uuid != packet.pokemonID) {
            return
        }
        if (party[packet.newPosition] != null) {
            // Should've been a swap, something is funky on the client
            return
        }
        party.move(pokemon, packet.newPosition)
    }
}