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
import com.cobblemon.mod.common.battles.ShowdownInterpreter
import com.cobblemon.mod.common.battles.dispatch.CauserInstruction
import com.cobblemon.mod.common.battles.dispatch.GO
import com.cobblemon.mod.common.battles.dispatch.InstructionSet
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.battles.dispatch.WaitDispatch
import com.cobblemon.mod.common.util.battleLang

/**
 * Format: |-ability|POKEMON|ABILITY|(from)EFFECT
 *
 * The ABILITY of POKEMON has been changed due to a move/ability EFFECT.
 *
 * Alt format: |-ability|POKEMON|ABILITY
 *
 * POKEMON has just switched-in, and its ABILITY is being announced to have a long-term effect.
 * @author Xylopia
 * @since January 31st, 2023
 */
class AbilityInstruction(val instructionSet: InstructionSet, val message: BattleMessage) : InterpreterInstruction, CauserInstruction {
    override fun invoke(battle: PokemonBattle) {
        val pokemon = message.battlePokemon(0, battle) ?: return
        val effect = message.effectAt(1) ?: return
        val optionalEffect = message.effect()
        val optionalPokemon = message.battlePokemonFromOptional(battle)

        // If there is an optional effect causing the activation, broadcast that instead of the standard effect
        ShowdownInterpreter.broadcastAbility(battle, optionalEffect ?: effect, pokemon)

        battle.dispatch {
            val pokemonName = pokemon.getName()
            val optionalPokemonName = optionalPokemon?.getName()
            ShowdownInterpreter.lastCauser[battle.battleId] = message

            val lang = when (optionalEffect?.id) {
                "trace" -> optionalPokemonName?.let { battleLang("ability.trace", pokemonName, it, effect.typelessData) }
                "receiver", "powerofalchemy" -> optionalPokemonName?.let { battleLang("ability.receiver", it, effect.typelessData) } // Receiver and Power of Alchemy share the same text
                else -> when (effect.id) {
                    "sturdy", "unnerve", "anticipation" -> battleLang("ability.${effect.id}", pokemonName) // Unique message
                    "airlock", "cloudnine" -> battleLang("ability.airlock") // Cloud Nine shares the same text as Air Lock
                    else -> null // Effect broadcasted by a succeeding instruction
                }
            }

            battle.minorBattleActions[pokemon.uuid] = message
            if (lang != null) {
                battle.broadcastChatMessage(lang)
                return@dispatch WaitDispatch(1F)
            }
            else return@dispatch GO
        }
    }
}