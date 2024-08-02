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
import com.cobblemon.mod.common.battles.BattleSide
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction

/**
 * Format: |-swapsideconditions
 *
 * Swaps side conditions between sides. Used for Court Change.
 * @author Segfault Guy
 * @since February 19th, 2024
 */
class SwapSideConditionsInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        battle.dispatchGo {
            val sides = mutableListOf<BattleSide>()
            battle.sides.forEach { side ->
                if (!sides.contains(side)) {
                    val oppositeManager = side.getOppositeSide().contextManager
                    side.contextManager.swap(oppositeManager, BattleContext.Type.TAILWIND, BattleContext.Type.SCREEN, BattleContext.Type.HAZARD)
                }
                sides.add(side)
            }
        }
    }
}