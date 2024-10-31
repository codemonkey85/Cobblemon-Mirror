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
import com.cobblemon.mod.common.battles.ShowdownInterpreter
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction

/**
 * Format: |-item|POKEMON|ITEM|(from)EFFECT
 *
 * ITEM held by POKEMON has been changed or revealed due to a move or ability EFFECT.
 *
 * Alt format: |-item|POKEMON|ITEM
 *
 * POKEMON has just switched in, and its ITEM is being announced to have a long-term effect.
 * @author Licious
 * @since December 30th, 2022
 */
class ItemInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        val source = message.battlePokemonFromOptional(battle)
        source?.let { ShowdownInterpreter.broadcastOptionalAbility(battle, message.effect(), source) }

        battle.dispatchWaiting(1.5F) {
            val battlePokemon = message.battlePokemon(0, battle) ?: return@dispatchWaiting
            battlePokemon.heldItemManager.handleStartInstruction(battlePokemon, battle, message)
            battle.minorBattleActions[battlePokemon.uuid] = message
            battlePokemon.contextManager.add(ShowdownInterpreter.getContextFromAction(message, BattleContext.Type.ITEM, battle))
        }
    }
}