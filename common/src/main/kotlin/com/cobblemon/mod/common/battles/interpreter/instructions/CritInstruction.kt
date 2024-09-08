/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles.interpreter.instructions

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.util.battleLang
import com.cobblemon.mod.common.pokemon.evolution.progress.LastBattleCriticalHitsEvolutionProgress
import com.cobblemon.mod.common.battles.ShowdownInterpreter
import com.cobblemon.mod.common.battles.dispatch.InstructionSet

/**
 * Format: |-crit|POKEMON
 *
 * POKEMON received a critical hit.
 * @author Hunter
 * @since August 18th, 2022
 */
class CritInstruction(val message: BattleMessage, val instructionSet: InstructionSet,
): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        val lastCauser  = instructionSet.getMostRecentCauser(comparedTo = this)
        battle.dispatchGo {
            val pokemon = message.battlePokemon(0, battle) ?: return@dispatchGo
            ShowdownInterpreter.lastCauser[battle.battleId]?.let { message ->
                val battlePokemon = message.battlePokemon(0, battle) ?: return@let
                if (lastCauser is MoveInstruction && lastCauser.spreadTargets.isNotEmpty()) {
                    val pokemonName = battlePokemon.getName()
                    battle.broadcastChatMessage(battleLang("crit_spread", pokemonName).yellow())
                } else {
                    battle.broadcastChatMessage(battleLang("crit").yellow())
                }
                if (LastBattleCriticalHitsEvolutionProgress.supports(battlePokemon.effectedPokemon)) {
                    val progress = battlePokemon.effectedPokemon.evolutionProxy.current().progressFirstOrCreate({ it is LastBattleCriticalHitsEvolutionProgress }) { LastBattleCriticalHitsEvolutionProgress() }
                    progress.updateProgress(LastBattleCriticalHitsEvolutionProgress.Progress(progress.currentProgress().amount + 1))
                }
            }
            battle.minorBattleActions[pokemon.uuid] = message
        }
    }
}