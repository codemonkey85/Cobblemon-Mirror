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
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.util.battleLang

/**
 * Format: |-prepare|ATTACKER|MOVE and |-prepare|ATTACKER|MOVE|DEFENDER
 *
 * ATTACKER Pokémon is preparing to use a charge MOVE on DEFENDER or an unknown target.
 * @author Renaissance
 * @since March 24th, 2023
 */
class PrepareInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        battle.dispatchWaiting(1.5F) {
            val pokemon = message.battlePokemon(0, battle) ?: return@dispatchWaiting
            val pokemonName = pokemon.getName()
            val effectID = message.effectAt(1)?.id ?: return@dispatchWaiting
            //Prevents spam when the move Role Play is used
            val lang = when (effectID) {
                "shadowforce" -> battleLang("prepare.phantomforce", pokemonName) //Phantom Force and Shadow Force share the same text
                "solarblade" -> battleLang("prepare.solarbeam", pokemonName) //Solar Beam and Solar Blade share the same text
                else -> battleLang("prepare.$effectID", pokemonName)
            }
            battle.broadcastChatMessage(lang)
            battle.minorBattleActions[pokemon.uuid] = message
        }
    }
}