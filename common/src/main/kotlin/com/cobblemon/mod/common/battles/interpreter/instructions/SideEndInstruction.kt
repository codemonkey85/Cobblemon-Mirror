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
 * Format: |-sideend|SIDE|CONDITION
 *
 * Indicates that the side CONDITION ended for the given SIDE.
 * @author Segfault Guy
 * @since April 5th, 2023
 */
class SideEndInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        battle.dispatchWaiting(2F) {
            val side = if (message.argumentAt(0)?.get(1) == '1') battle.side1 else battle.side2
            val effect = message.effectAt(1) ?: return@dispatchWaiting
            battle.sides.forEach {
                val subject = if (it == side) battleLang("side_subject.ally") else battleLang("side_subject.opponent")
                val lang = battleLang("sideend.${effect.id}", subject)
                it.broadcastChatMessage(lang)
            }

            val bucket = when(effect.rawData.substringAfterLast(" ").lowercase()) {
                "reflect", "screen", "veil" -> BattleContext.Type.SCREEN
                "spikes", "rock", "web" -> BattleContext.Type.HAZARD
                "tailwind" -> BattleContext.Type.TAILWIND
                else -> BattleContext.Type.MISC
            }
            side.contextManager.remove(effect.id, bucket)
        }
    }
}