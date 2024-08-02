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
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.util.battleLang

/**
 * Format: |-hitcount|POKEMON|NUM
 *
 * A multi-hit move hit the POKEMON NUM times.
 * @author Licious
 * @since December 30th, 2022
 */
class HitCountInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        battle.dispatchGo {
            val battlePokemon = message.battlePokemon(0, battle) ?: return@dispatchGo
            val hitCount = message.argumentAt(1)?.toIntOrNull() ?: return@dispatchGo
            val lang = if (hitCount == 1) battleLang("hit_count_singular") else battleLang("hit_count", hitCount)
            battle.minorBattleActions[battlePokemon.uuid] = message
            battle.broadcastChatMessage(lang)
        }
    }
}