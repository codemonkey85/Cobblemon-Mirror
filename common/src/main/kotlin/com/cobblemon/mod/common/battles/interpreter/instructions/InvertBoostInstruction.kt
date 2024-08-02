/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles.interpreter.instructions

import com.cobblemon.mod.common.api.battles.interpreter.BasicContext
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.battles.ShowdownInterpreter
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.util.battleLang

/**
 * Format: |-invertboost|POKEMON
 *
 * POKEMON had its stat changes inverted.
 * @author JMMCP
 * @since November 26th, 2023
 */
class InvertBoostInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        battle.dispatchWaiting {
            val pokemon = message.battlePokemon(0, battle) ?: return@dispatchWaiting
            val name = pokemon.getName()
            battle.broadcastChatMessage(battleLang("invertboost", name))

            // update and invert BOOST and UNBOOST contexts
            val context = ShowdownInterpreter.getContextFromAction(message, BattleContext.Type.BOOST, battle)
            val newUnboosts = pokemon.contextManager.get(BattleContext.Type.BOOST)?.map {
                BasicContext(it.id, context.turn, BattleContext.Type.UNBOOST, context.origin)
            }?.toTypedArray()
            val newBoosts = pokemon.contextManager.get(BattleContext.Type.UNBOOST)?.map {
                BasicContext(it.id, context.turn, BattleContext.Type.BOOST, context.origin)
            }?.toTypedArray()

            pokemon.contextManager.clear(BattleContext.Type.BOOST, BattleContext.Type.UNBOOST)
            newBoosts?.let { pokemon.contextManager.add(*it) }
            newUnboosts?.let { pokemon.contextManager.add(*it) }
            battle.minorBattleActions[pokemon.uuid] = message
        }
    }
}