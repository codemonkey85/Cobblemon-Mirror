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
 * Format: |-resisted|POKEMON
 *
 * POKEMON resisted the attack.
 * @author Hunter
 * @since August 18th, 2022
 */
class ResistedInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        battle.dispatchGo {
            val pokemon = message.battlePokemon(0, battle) ?: return@dispatchGo
            battle.broadcastChatMessage(battleLang("resisted"))
            battle.minorBattleActions[pokemon.uuid] = message
        }
    }
}