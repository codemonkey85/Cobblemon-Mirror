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
import com.cobblemon.mod.common.util.battleLang

/**
 * Format: |-weather|WEATHER
 *
 * Indicates WEATHER is currently in effect.
 *
 * If upkeep is present, it means that WEATHER was active previously and is still in effect that turn.
 * Otherwise, it means that the weather has changed due to a move or ability, or has expired, in which case WEATHER will be none.
 * @author Hunter
 * @since September 25th, 2022
 */
class WeatherInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        val weather = message.effectAt(0)?.id ?: return
        val source = message.battlePokemonFromOptional(battle)
        source?.let { ShowdownInterpreter.broadcastOptionalAbility(battle, message.effect(), source) }

        battle.dispatchWaiting(1.5F) {
            val lang = when {
                message.hasOptionalArgument("upkeep") -> battleLang("weather.$weather.upkeep")
                weather != "none" -> {
                    battle.contextManager.add(ShowdownInterpreter.getContextFromAction(message, BattleContext.Type.WEATHER, battle))
                    battleLang("weather.$weather.start")
                }
                else -> {
                    val oldWeather = battle.contextManager.get(BattleContext.Type.WEATHER)?.iterator()?.next()?.id ?: return@dispatchWaiting
                    battle.contextManager.clear(BattleContext.Type.WEATHER)
                    battleLang("weather.$oldWeather.end")
                }
            }
            battle.broadcastChatMessage(lang)
        }
    }
}