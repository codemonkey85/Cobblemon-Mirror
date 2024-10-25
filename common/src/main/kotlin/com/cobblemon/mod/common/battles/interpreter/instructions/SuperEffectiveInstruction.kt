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
import com.cobblemon.mod.common.battles.dispatch.InstructionSet
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.util.battleLang

/**
 * Format: |-supereffective|POKEMON
 *
 * A move was super effective against POKEMON.
 * @author Hunter
 * @since August 18th, 2022
 */
class SuperEffectiveInstruction(val message: BattleMessage,val instructionSet: InstructionSet,
): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        val lastCauser = instructionSet.getMostRecentCauser(comparedTo = this)
        battle.dispatchGo {
            val pokemon = message.battlePokemon(0, battle) ?: return@dispatchGo
            if (lastCauser is MoveInstruction && lastCauser.spreadTargets.isNotEmpty()) {
                val pokemonName = pokemon.getName()
                battle.broadcastChatMessage(battleLang("superEffective_spread", pokemonName))
            } else {
                battle.broadcastChatMessage(battleLang("superEffective"))
            }
            battle.minorBattleActions[pokemon.uuid] = message
        }
    }
}