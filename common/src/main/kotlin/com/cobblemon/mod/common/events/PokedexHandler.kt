/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.events

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.PokemonGainedEvent
import com.cobblemon.mod.common.api.events.pokemon.PokemonSeenEvent
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreType
import com.cobblemon.mod.common.net.messages.client.SetClientPlayerDataPacket
import com.cobblemon.mod.common.util.getPlayer

object PokedexHandler : EventHandler {
    override fun registerListeners() {
        CobblemonEvents.POKEMON_GAINED.subscribe(Priority.NORMAL, ::onPokemonGained)
        CobblemonEvents.POKEMON_SEEN.subscribe(Priority.NORMAL, ::onPokemonSeen)
    }

    fun onPokemonGained(event: PokemonGainedEvent) {
        //We should make it so this works for offline players better
        val player = event.playerId.getPlayer() ?: return
        val playerPokedex = Cobblemon.playerDataManager.getPokedexData(player)
        playerPokedex.gainedCaughtStatus(event.pokemon)
        player.sendPacket(SetClientPlayerDataPacket(
            PlayerInstancedDataStoreType.POKEDEX,
            playerPokedex.toClientData()
        ))
    }

    fun onPokemonSeen(event: PokemonSeenEvent) {
        val player = event.playerId.getPlayer() ?: return
        val playerPokedex = Cobblemon.playerDataManager.getPokedexData(player)
        playerPokedex.gainedCaughtStatus(event.pokemon)
        player.sendPacket(SetClientPlayerDataPacket(
            PlayerInstancedDataStoreType.POKEDEX,
            playerPokedex.toClientData()
        ))
    }
}