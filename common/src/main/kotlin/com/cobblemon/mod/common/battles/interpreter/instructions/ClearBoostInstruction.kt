/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles.interpreter.instructions

import com.cobblemon.mod.common.api.battles.interpreter.BattleContext
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.util.battleLang

/**
 * Format: |-clearboost|POKEMON
 *
 * Clear the boosts from the target POKEMON.
 * @author Yaseen
 * @since October 26th, 2024
 */
class ClearBoostInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        val battlePokemon = message.battlePokemon(0, battle) ?: return

        battle.dispatchWaiting(1.5F) {
            val pokemonName = battlePokemon.getName()
            val lang = battleLang("clearboost", pokemonName)
            battle.broadcastChatMessage(lang)
            battlePokemon.contextManager.clear(BattleContext.Type.BOOST, BattleContext.Type.UNBOOST)
            battle.minorBattleActions[battlePokemon.uuid] = message
        }
    }
}