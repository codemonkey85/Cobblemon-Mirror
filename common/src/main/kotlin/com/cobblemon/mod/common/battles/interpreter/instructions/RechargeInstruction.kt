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
 * Format: |-mustrecharge|POKEMON
 *
 * POKEMON must spend the turn recharging from a previous move.
 * @author Hunter
 * @since September 25th, 2022
 */
class RechargeInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        battle.dispatchWaiting(2F){
            val pokemon = message.battlePokemon(0, battle) ?: return@dispatchWaiting
            battle.broadcastChatMessage(battleLang("recharge", pokemon.getName()))
            battle.minorBattleActions[pokemon.uuid] = message
        }
    }
}