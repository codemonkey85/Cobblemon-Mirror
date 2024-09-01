/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.storage.pc

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.storage.ReleasePokemonEvent
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.client.settings.ServerSettings
import com.cobblemon.mod.common.net.messages.server.storage.party.ReleasePartyPokemonPacket
import com.cobblemon.mod.common.util.party
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object ReleasePartyPokemonHandler : ServerNetworkPacketHandler<ReleasePartyPokemonPacket> {
    override fun handle(packet: ReleasePartyPokemonPacket, server: MinecraftServer, player: ServerPlayer) {
        val party = player.party()
        val pokemon = party[packet.position] ?: return
        if (pokemon.uuid != packet.pokemonID) {
            return // Desync
        }
CobblemonEvents.POKEMON_RELEASED_EVENT_PRE.postThen(
                event = ReleasePokemonEvent.Pre(player, pokemon, party),
                ifSucceeded = { preEvent ->        if (ServerSettings.preventCompletePartyDeposit && party.filterNotNull().size <= 1) {
            return // Don't allow empty party
        }
                    party.remove(pokemon)
                    CobblemonEvents.POKEMON_RELEASED_EVENT_POST.post(ReleasePokemonEvent.Post(player, pokemon, party))
                },
                ifCanceled = { preEvent ->
                    party[packet.position] = pokemon
                }
        )
    }
}