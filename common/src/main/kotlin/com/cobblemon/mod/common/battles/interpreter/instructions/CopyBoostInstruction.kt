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
 * Format: |-copyboost|SOURCE|TARGET|(from)EFFECT
 *
 * Copies any stat changes from the TARGET Pokémon to the SOURCE Pokémon due to EFFECT.
 * @author JMMCP
 * @since November 27th, 2023
 */
class CopyBoostInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        battle.dispatchWaiting {
            val pokemon = message.battlePokemon(0, battle) ?: return@dispatchWaiting
            val pokemonName = pokemon.getName()
            val targetPokemon = message.battlePokemon(1, battle) ?: return@dispatchWaiting
            val targetPokemonName = targetPokemon.getName()
            val lang = battleLang("copyboost.generic", pokemonName, targetPokemonName)
            battle.broadcastChatMessage(lang)

            pokemon.contextManager.copy(targetPokemon.contextManager, BattleContext.Type.BOOST, BattleContext.Type.UNBOOST)
            battle.minorBattleActions[pokemon.uuid] = message
        }
    }
}