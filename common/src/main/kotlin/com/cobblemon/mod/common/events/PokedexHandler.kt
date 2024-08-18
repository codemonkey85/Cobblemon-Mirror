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
import com.cobblemon.mod.common.api.events.battles.BattleStartedPostEvent
import com.cobblemon.mod.common.api.events.pokedex.scanning.PokemonScannedEvent
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent
import com.cobblemon.mod.common.api.events.pokemon.TradeCompletedEvent
import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionCompleteEvent
import com.cobblemon.mod.common.api.events.starter.StarterChosenEvent
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreType
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor
import com.cobblemon.mod.common.net.messages.client.SetClientPlayerDataPacket
import com.cobblemon.mod.common.util.getPlayer

//TODO: Figure out how to do incremental updates
//How do we track the changes through 3 levels of the dex
object PokedexHandler {
    fun onCapture(event : PokemonCapturedEvent) {
        val pokedexData = Cobblemon.playerDataManager.getPokedexData(event.player)
        pokedexData.pokemonCaught(event)
        event.player.sendPacket(SetClientPlayerDataPacket(PlayerInstancedDataStoreType.POKEDEX, pokedexData.toClientData(), false))
    }

    fun onScan(event: PokemonScannedEvent) {
        val pokedexData = Cobblemon.playerDataManager.getPokedexData(event.player)
        pokedexData.pokemonScanned(event)
        event.player.sendPacket(SetClientPlayerDataPacket(PlayerInstancedDataStoreType.POKEDEX, pokedexData.toClientData(), false))
    }

    fun onEvolve(event: EvolutionCompleteEvent) {
        val ownedBy = event.pokemon.getOwnerPlayer()
        if(ownedBy == null){
            Cobblemon.LOGGER.warn("Evolved ${event.pokemon.species} that is not owned by any player. Stat was not tracked.")
            return
        }

        val pokedexData = Cobblemon.playerDataManager.getPokedexData(ownedBy)
        pokedexData.pokemonEvolved(event)
        ownedBy.sendPacket(SetClientPlayerDataPacket(PlayerInstancedDataStoreType.POKEDEX, pokedexData.toClientData(), false))
    }

    fun onTrade(event: TradeCompletedEvent){
        var player = event.tradeParticipant1.uuid.getPlayer()
        if(player == null){
            Cobblemon.LOGGER.warn("Player with UUID '${event.tradeParticipant1.uuid} could not be found.")
        } else {
            val pokedexData = Cobblemon.playerDataManager.getPokedexData(player)
            pokedexData.pokemonTraded(event)
            player.sendPacket(SetClientPlayerDataPacket(PlayerInstancedDataStoreType.POKEDEX, pokedexData.toClientData(), false))
        }

        player = event.tradeParticipant2.uuid.getPlayer()
        if(player == null){
            Cobblemon.LOGGER.warn("Player with UUID '${event.tradeParticipant2.uuid} could not be found.")
        } else {
            val pokedexData = Cobblemon.playerDataManager.getPokedexData(player)
            pokedexData.pokemonTraded(event)
            player.sendPacket(SetClientPlayerDataPacket(PlayerInstancedDataStoreType.POKEDEX, pokedexData.toClientData(), false))
        }
    }

    fun onBattleStart(event: BattleStartedPostEvent){
        if (event.battle.isPvW) {
            val player = event.battle.players.first()
            val pokedex = Cobblemon.playerDataManager.getPokedexData(player)
            pokedex.battleStart(event)
            player.sendPacket(SetClientPlayerDataPacket(PlayerInstancedDataStoreType.POKEDEX, pokedex.toClientData(), false))
        }
    }

    fun onStarterSelect(event: StarterChosenEvent) {
        val pokedexData = Cobblemon.playerDataManager.getPokedexData(event.player)
        pokedexData.onStarterChosen(event)
        event.player.sendPacket(SetClientPlayerDataPacket(PlayerInstancedDataStoreType.POKEDEX, pokedexData.toClientData(), false))
    }
}