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
import com.cobblemon.mod.common.battles.dispatch.GO
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction

/**
 * Format: |pp_update|<side_id>: <pokemon_uuid>|...<move_id>: <move_pp>
 *
 * @author Licious
 * @since September 22nd, 2022
 */
class PpUpdateInstruction( val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        battle.dispatch {
            val pokemon = message.battlePokemon(0, battle) ?: return@dispatch GO
            val moveDatum = message.argumentAt(1)?.split(", ") ?: return@dispatch GO
            moveDatum.forEach { moveData ->
                val moveIdAndPp = moveData.split(": ")
                val moveId = moveIdAndPp[0]
                val movePp = moveIdAndPp[1]
                val move = pokemon.effectedPokemon.moveSet.firstOrNull { move -> move.name.equals(moveId, true) } ?: return@dispatch GO
                move.currentPp = movePp.toInt()
            }
            GO
        }
    }
}