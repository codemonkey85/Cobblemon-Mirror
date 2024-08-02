/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles.interpreter.instructions

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.battles.dispatch.GO
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction

/**
 * Format: |upkeep|
 *
 * Signals the upkeep phase of the turn where the number of turns left for field conditions are updated.
 * @author Hiroku
 * @since March 12th, 2022
 */
class UpkeepInstruction : InterpreterInstruction {
    override fun invoke(battle: PokemonBattle) {
        battle.dispatch {
            battle.actors.forEach { it.upkeep() }
            GO
        }
    }
}