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
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.battles.ShowdownInterpreter
import com.cobblemon.mod.common.battles.dispatch.InstructionSet
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.util.battleLang

/**
 * Format: |-fieldactivate|EFFECT
 *
 * A miscellaneous EFFECT has activated for the entire field.
 * @author Segfault Guy
 * @since April 5th, 2023
 */
class FieldActivateInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        battle.dispatchWaiting(2.5F) {
            val effectID = message.effectAt(0)?.id ?: return@dispatchWaiting
            val lang = battleLang("fieldactivate.$effectID")
            battle.broadcastChatMessage(lang.red())

            // share this action with all active Pokemon
            battle.activePokemon.forEach {
                it.battlePokemon?.contextManager?.addUnique(ShowdownInterpreter.getContextFromAction(message, BattleContext.Type.VOLATILE, battle))
            }
        }
    }
}