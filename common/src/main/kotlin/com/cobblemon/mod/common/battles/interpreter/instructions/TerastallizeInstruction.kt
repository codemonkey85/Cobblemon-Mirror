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
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.battles.instruction.TerastallizationEvent
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.api.types.tera.TeraTypes
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.util.battleLang

/**
 * Format: |-terastallize|POKEMON|TYPE
 *
 * POKEMON terastallized into TYPE.
 * @author Segfault Guy
 * @since September 10th, 2023
 */
class TerastallizeInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        val battlePokemon = message.battlePokemon(0, battle) ?: return
        val type = message.effectAt(1)?.let { TeraTypes.get(it.id) } ?: return
        battle.dispatchWaiting {
            val pokemonName = battlePokemon.getName()
            battle.broadcastChatMessage(battleLang("terastallize", pokemonName, type.displayName).yellow())
            CobblemonEvents.TERASTALLIZATION.post(TerastallizationEvent(battle, battlePokemon, type))
            battle.minorBattleActions[battlePokemon.uuid] = message
        }
    }
}
