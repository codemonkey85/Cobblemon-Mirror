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
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.battles.dispatch.InstructionSet
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.util.battleLang

/**
 * Format: |-candynamax|SIDE
 *
 * SIDE can choose to dynamax this turn.
 * In multi battles, actors on a team are alternatively given the option to dynamax each turn.
 * On turn 1, the actors on their team's respective left have the first chance (p1 and p2; in multi battles, each actor is their own side).
 * @author Segfault Guy
 * @since July 16th, 2024
 */
class CanDynamaxInstruction(val instructionSet: InstructionSet, val battleActor: BattleActor, val publicMessage: BattleMessage, val privateMessage: BattleMessage): InterpreterInstruction {
    override fun invoke(battle: PokemonBattle) {
        battle.dispatchWaiting {
            val side = privateMessage.argumentAt(0) ?: return@dispatchWaiting
            battle.getActor(side)?.let {
                it.canDynamax = true
                battle.broadcastChatMessage(battleLang("candynamax", it.getName()).yellow())
            }
        }
    }
}