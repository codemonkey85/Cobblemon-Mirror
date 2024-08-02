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
import com.cobblemon.mod.common.battles.dispatch.InstructionSet
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.util.battleLang
import com.cobblemon.mod.common.util.lang

/**
 * Format: |-end|POKEMON|EFFECT
 *
 * The volatile status from EFFECT inflicted on POKEMON has ended.
 * @author Hiroku
 * @since October 3rd, 2022
 */
class EndInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        battle.dispatchWaiting {
            val pokemon = message.battlePokemon(0, battle) ?: return@dispatchWaiting
            val pokemonName = pokemon.getName()
            val effectID = message.effectAt(1)?.id ?: return@dispatchWaiting
            if (!message.hasOptionalArgument("silent")) {
                val lang = when (effectID) {
                    "yawn" -> lang("status.sleep.apply", pokemonName)
                    else -> battleLang("end.$effectID", pokemonName)
                }
                battle.broadcastChatMessage(lang)
            }
            pokemon.contextManager.remove(effectID, BattleContext.Type.VOLATILE)
            battle.minorBattleActions[pokemon.uuid] = message
        }
    }
}