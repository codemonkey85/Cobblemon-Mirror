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
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.battles.ShowdownInterpreter
import com.cobblemon.mod.common.battles.dispatch.GO
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.battles.dispatch.WaitDispatch
import com.cobblemon.mod.common.util.battleLang
import net.minecraft.network.chat.Component

/**
 * Format: |-start|POKEMON|EFFECT
 *
 * A volatile status has been inflicted on POKEMON by EFFECT.
 * @author Deltric
 * @since January 21st, 2022
 */
class StartInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        battle.dispatch {
            val pokemon = message.battlePokemon(0, battle) ?: return@dispatch GO
            val effectID = message.effectAt(1)?.id ?: return@dispatch GO

            val optionalEffect = message.effect()
            val optionalPokemon = message.battlePokemonFromOptional(battle)
            val optionalPokemonName = optionalPokemon?.getName()
            val extraEffect = message.effectAt(2)?.typelessData ?: Component.literal("UNKOWN")

            // skip adding contexts for every time the perish counter decrements
            if (!effectID.contains("perish")) {
                // don't need to add unique: showdown won't send -start instruction if volatile status is already present
                pokemon.contextManager.add(ShowdownInterpreter.getContextFromAction(message, BattleContext.Type.VOLATILE, battle))
            }
            battle.minorBattleActions[pokemon.uuid] = message

            if (!message.hasOptionalArgument("silent")) {
                val lang = if (optionalEffect?.id == "reflecttype" && optionalPokemonName != null)
                    battleLang("start.reflecttype", pokemon.getName(), optionalPokemonName)
                else
                    when (effectID) {
                        "perish3" -> return@dispatch GO // Skip
                        "perish2", "perish1", "perish0",
                        "stockpile1", "stockpile2", "stockpile3" -> battleLang("start.${effectID.dropLast(1)}", pokemon.getName(), effectID.last().digitToInt())
                        "dynamax" -> battleLang("start.${message.effectAt(2)?.id ?: effectID}", pokemon.getName()).yellow()
                        "curse" -> battleLang("start.curse", message.battlePokemonFromOptional(battle)!!.getName(), pokemon.getName())
                        else -> battleLang("start.$effectID", pokemon.getName(), extraEffect)
                    }
                battle.broadcastChatMessage(lang)
            }
            WaitDispatch(1F)
        }
    }
}