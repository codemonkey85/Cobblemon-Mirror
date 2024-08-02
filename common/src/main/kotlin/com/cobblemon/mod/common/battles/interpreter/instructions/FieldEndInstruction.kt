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
 * Format: |-fieldend|CONDITION
 *
 * The field CONDITION has ended.
 * @author Licious
 * @since February 8th, 2023
 */
class FieldEndInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        battle.dispatchWaiting(1.5F) {
            val effect = message.effectAt(0) ?: return@dispatchWaiting
            val lang = battleLang("fieldend.${effect.id}")
            battle.broadcastChatMessage(lang)

            val type = BattleContext.Type.valueOf(effect.rawData.substringAfterLast(" ").uppercase())
            battle.contextManager.remove(effect.id, type)
        }
    }
}