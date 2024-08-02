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
 * Format: |detailschange|POKEMON|DETAILS|HP STATUS
 *
 * POKEMON has changed formes permanently (i.e. Mega Evolution) to DETAILS.
 * @author Segfault Guy
 * @since September 10th, 2023
 */
class DetailsChangeInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        val battlePokemon = message.battlePokemon(0, battle) ?: return
        val formName = message.argumentAt(1)?.split(',')?.get(0)?.substringAfter('-')?.lowercase() ?: return
        battle.dispatchWaiting {
            val pokemonName = battlePokemon.getName()
            battle.broadcastChatMessage(battleLang("detailschange.$formName", pokemonName))
            battle.majorBattleActions[battlePokemon.uuid] = message
        }
    }
}