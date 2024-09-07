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
 * Format: |-resisted|POKEMON
 *
 * POKEMON resisted the attack.
 * @author Hunter
 * @since August 18th, 2022
 */
class ResistedInstruction(
    val publicMessage: BattleMessage,
    val instructionSet: InstructionSet,
) : InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        val battlePokemon = publicMessage.battlePokemon(0, battle)
        battlePokemon ?: return
        val lastCauser  = instructionSet.getMostRecentCauser(comparedTo = this)
        battle.dispatchGo {
            if (lastCauser is MoveInstruction && lastCauser.spreadTargets.isNotEmpty()) {
                val pokemonName = battlePokemon.getName()
                battle.broadcastChatMessage(battleLang("resisted_spread", pokemonName))
            } else {
                battle.broadcastChatMessage(battleLang("resisted"))
            }
            battle.minorBattleActions[battlePokemon.uuid] = publicMessage
        }
    }
}