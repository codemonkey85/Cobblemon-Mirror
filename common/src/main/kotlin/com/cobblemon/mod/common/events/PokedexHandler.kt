package com.cobblemon.mod.common.events

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.PokemonGainedEvent
import com.cobblemon.mod.common.api.events.pokemon.PokemonSeenEvent
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
    }

    fun onPokemonSeen(event: PokemonSeenEvent) {
        val player = event.playerId.getPlayer() ?: return
        val playerPokedex = Cobblemon.playerDataManager.getPokedexData(player)
        playerPokedex.gainedCaughtStatus(event.pokemon)
    }
}