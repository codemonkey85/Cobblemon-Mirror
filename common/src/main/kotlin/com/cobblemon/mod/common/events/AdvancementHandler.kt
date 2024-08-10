/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.events

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.advancement.CobblemonCriteria
import com.cobblemon.mod.common.advancement.criterion.*
import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent
import com.cobblemon.mod.common.api.events.pokemon.LevelUpEvent
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent
import com.cobblemon.mod.common.api.events.pokemon.TradeCompletedEvent
import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionCompleteEvent
import com.cobblemon.mod.common.block.TumblestoneBlock
import com.cobblemon.mod.common.item.TumblestoneItem
import com.cobblemon.mod.common.platform.events.ServerPlayerEvent
import com.cobblemon.mod.common.util.getPlayer
import java.util.*

object AdvancementHandler {

    fun onCapture(event : PokemonCapturedEvent) {
        val playerData = Cobblemon.playerData.get(event.player)
        val advancementData = playerData.advancementData
        advancementData.updateTotalCaptureCount()
        advancementData.updateAspectsCollected(event.player, event.pokemon)
        CobblemonCriteria.CATCH_POKEMON.trigger(event.player, CountablePokemonTypeContext(advancementData.totalCaptureCount, "any"))
        event.pokemon.types.forEach {
            advancementData.updateTotalTypeCaptureCount(it)
            CobblemonCriteria.CATCH_POKEMON.trigger(event.player, CountablePokemonTypeContext(advancementData.getTotalTypeCaptureCount(it), it.name))
        }
        if (event.pokemon.shiny) {
            advancementData.updateTotalShinyCaptureCount()
            CobblemonCriteria.CATCH_SHINY_POKEMON.trigger(event.player, advancementData.totalShinyCaptureCount)
        }
        CobblemonCriteria.COLLECT_ASPECT.trigger(event.player, advancementData.aspectsCollected)
        Cobblemon.playerData.saveSingle(playerData)
    }

//    fun onHatch(event: HatchEggEvent) {
//        val playerData = Cobblemon.playerData.get(event.player)
//        val advancementData = playerData.advancementData
//        advancementData.updateTotalEggsHatched()
//        Cobblemon.playerData.saveSingle(playerData)
//        CobblemonCriteria.EGG_HATCH.trigger(event.player, advancementData.totalEggsHatched)
//    }

    fun onEvolve(event: EvolutionCompleteEvent) {
        val player = event.pokemon.getOwnerPlayer()
        if (player != null) {
            if (event.pokemon.preEvolution != null) {
                val playerData = Cobblemon.playerData.get(player)
                val advancementData = playerData.advancementData
                advancementData.updateTotalEvolvedCount()
                advancementData.updateAspectsCollected(player, event.pokemon)
                Cobblemon.playerData.saveSingle(playerData)
                CobblemonCriteria.EVOLVE_POKEMON.trigger(
                    player, EvolvePokemonContext(
                        event.pokemon.preEvolution!!.species.resourceIdentifier,
                        event.pokemon.species.resourceIdentifier,
                        advancementData.totalEvolvedCount
                    )
                )
                CobblemonCriteria.COLLECT_ASPECT.trigger(player, advancementData.aspectsCollected)
            }
            else {
                Cobblemon.LOGGER.warn("Evolution triggered by ${player.displayName} has missing evolution data for ${event.pokemon.species.resourceIdentifier}. Incomplete evolution data: ${event.evolution.id}, please report to the datapack creator!")
            }
        }
    }

    fun onWinBattle(event: BattleVictoryEvent) {
        if(!event.wasWildCapture) {
            if (event.battle.isPvW) {
                event.winners
                    .flatMap { it.getPlayerUUIDs().mapNotNull(UUID::getPlayer) }
                    .forEach { player ->
                        val playerData = Cobblemon.playerData.get(player)
                        val advancementData = playerData.advancementData
                        event.battle.actors.forEach { battleActor ->
                            if (!event.winners.contains(battleActor) && battleActor.type == ActorType.WILD) {
                                battleActor.pokemonList.forEach { battlePokemon ->
                                    advancementData.updateTotalDefeatedCount(battlePokemon.originalPokemon)
                                }
                            }
                        }
                        Cobblemon.playerData.saveSingle(playerData)
                        CobblemonCriteria.DEFEAT_POKEMON.trigger(player, advancementData.totalBattleVictoryCount)
                    }
            }
        }
        event.winners
            .flatMap { it.getPlayerUUIDs().mapNotNull(UUID::getPlayer) }
            .forEach { player ->
                val playerData = Cobblemon.playerData.get(player)
                val advancementData = playerData.advancementData
                advancementData.updateTotalBattleVictoryCount()
                if (event.battle.isPvW)
                    advancementData.updateTotalPvWBattleVictoryCount()
                if (event.battle.isPvP)
                    advancementData.updateTotalPvPBattleVictoryCount()
                if (event.battle.isPvN)
                    advancementData.updateTotalPvNBattleVictoryCount()
                Cobblemon.playerData.saveSingle(playerData)
                CobblemonCriteria.WIN_BATTLE.trigger(player, BattleCountableContext(advancementData.totalBattleVictoryCount, event.battle))
            }

    }

    fun onLevelUp(event : LevelUpEvent) {
        event.pokemon.getOwnerPlayer()?.let { CobblemonCriteria.LEVEL_UP.trigger(it, LevelUpContext(event.newLevel, event.pokemon)) }
    }

    fun onTradeCompleted(event : TradeCompletedEvent) {
        val player1 = event.tradeParticipant1Pokemon.getOwnerPlayer()
        val player2 = event.tradeParticipant2Pokemon.getOwnerPlayer()
        if (player1 != null) {
            CobblemonCriteria.TRADE_POKEMON.trigger(player1, TradePokemonContext(event.tradeParticipant1Pokemon, event.tradeParticipant2Pokemon))
            val playerData = Cobblemon.playerData.get(player1)
            val advancementData = playerData.advancementData
            advancementData.updateTotalTradedCount()
            advancementData.updateAspectsCollected(player1, event.tradeParticipant2Pokemon)
            CobblemonCriteria.COLLECT_ASPECT.trigger(player1, advancementData.aspectsCollected)
            Cobblemon.playerData.saveSingle(playerData)
        }
        if (player2 != null) {
            CobblemonCriteria.TRADE_POKEMON.trigger(player2, TradePokemonContext(event.tradeParticipant2Pokemon, event.tradeParticipant1Pokemon))
            val playerData = Cobblemon.playerData.get(player2)
            val advancementData = playerData.advancementData
            advancementData.updateTotalTradedCount()
            advancementData.updateAspectsCollected(player2, event.tradeParticipant1Pokemon)
            CobblemonCriteria.COLLECT_ASPECT.trigger(player2, advancementData.aspectsCollected)
            Cobblemon.playerData.saveSingle(playerData)
        }
    }

    /**
     * Triggers the advancement for placing a tumblestone
     *
     * Checks if the item in the player's hand is a tumblestone.
     * Then gets the block as a [TumblestoneBlock] so we can call the canGrow method.
     * Finally, triggers the advancement.
     *
     * @param event the event to trigger the advancement from
     */
    fun onTumbleStonePlaced(event: ServerPlayerEvent.RightClickBlock) {
        if (event.player.getStackInHand(event.hand).item is TumblestoneItem) {
            val block = ((event.player.getStackInHand(event.hand).item as TumblestoneItem).block as TumblestoneBlock)
            CobblemonCriteria.PLANT_TUMBLESTONE.trigger(event.player, PlantTumblestoneContext(event.pos, block))
        }
    }
}