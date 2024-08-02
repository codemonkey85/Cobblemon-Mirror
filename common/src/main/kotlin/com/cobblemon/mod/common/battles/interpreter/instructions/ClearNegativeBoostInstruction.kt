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
 * Format: |-clearnegativeboost|POKEMON
 *
 * Clear the negative boosts from the target POKEMON (usually as the result of a zeffect).
 * @author Segfault Guy
 * @since September 10th, 2023
 */
class ClearNegativeBoostInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        val battlePokemon = message.battlePokemon(0, battle) ?: return

        battle.dispatchWaiting(1.5F) {
            val pokemonName = battlePokemon.getName()
            val lang = when {
                message.hasOptionalArgument("zeffect") -> battleLang("clearallnegativeboost.zeffect", pokemonName)
                else -> battleLang("clearallnegativeboost", pokemonName)
            }
            if (!message.hasOptionalArgument("silent")) {
                battle.broadcastChatMessage(lang)
            }

            battlePokemon.contextManager.clear(BattleContext.Type.UNBOOST)
            battle.minorBattleActions[battlePokemon.uuid] = message
        }
    }
}