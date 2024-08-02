/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles.interpreter.instructions

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.util.battleLang

/**
 * Format: |-nothing
 *
 * Pathetic. Your move did absolutely nothing. You should feel bad for being bad.
 * @author Hiroku
 * @since August 20th, 2022
 */
class NothingInstruction() : InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        battle.dispatchGo { battle.broadcastChatMessage(battleLang("nothing")) }
    }
}