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
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.util.battleLang

/**
 * Format: |-fail|POKEMON|ACTION
 *
 * ACTION has failed against POKEMON.
 * @author Hunter
 * @since September 25th, 2022
 */
class FailInstruction(val message: BattleMessage): InterpreterInstruction {

    override fun invoke(battle: PokemonBattle) {
        battle.dispatchWaiting(1.5F){
            val pokemon = message.battlePokemon(0, battle) ?: return@dispatchWaiting
            val pokemonName = pokemon.getName()
            val effectID = message.effectAt(1)?.id
            val cause = message.effect("from")
            val of = message.battlePokemonFromOptional(battle)

            val lang = when (effectID) {
                null, "burnup", "doubleshock", "allyswitch" -> battleLang("fail") // Moves that use default fail lang. (Null included for moves that fail with no effect, for example: Baton Pass.)
                "shedtail" -> battleLang("fail.substitute", pokemonName)
                "hyperspacefury", "aurawheel" -> battleLang("fail.darkvoid", pokemonName) // Moves that can only be used by one species and fail when any others try
                "corrosivegas" -> battleLang("fail.healblock", pokemonName)
                "dynamax" -> battleLang("fail.grassknot", pokemonName) // Covers weight moves that fail against dynamaxed Pokémon
                "unboost" -> {
                    val statKey = message.argumentAt(2)
                    val stat = statKey?.let { Stats.getStat(it).displayName }
                    if (stat != null) {
                        battleLang("fail.$effectID.single", pokemonName, stat)
                    } else {
                        battleLang("fail.$effectID", pokemonName)
                    }
                }
                else -> battleLang("fail.$effectID", pokemonName)
            }
            battle.broadcastChatMessage(lang.red())
            battle.minorBattleActions[pokemon.uuid] = message
        }
    }
}