/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.stats

import com.cobblemon.mod.common.api.tags.CobblemonItemTags
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon

/**
 * Responsible for resolving EV yield after each battle.
 * For default implementation see [Generation8EvCalculator].
 *
 * @author Licious
 * @since October 31st, 2022
 */
interface EvCalculator {

    /**
     * TODO
     *
     * @param battlePokemon
     * @return
     */
    fun calculate(battlePokemon: BattlePokemon): Map<Stat, Int> {
        val total = hashMapOf<Stat, Int>()
        battlePokemon.facedOpponents
            .filter { it.health == 0 }
            .forEach { opponent ->
                val results = this.calculate(battlePokemon, opponent)
                results.forEach { (stat, value) ->
                    var newValue = total[stat] ?: 0
                    newValue += value
                    total[stat] = newValue
                }
            }
        return total
    }

    /**
     * TODO
     *
     * @param battlePokemon
     * @param opponentPokemon
     * @return
     */
    fun calculate(battlePokemon: BattlePokemon, opponentPokemon: BattlePokemon): Map<Stat, Int>

}

object Generation8EvCalculator : EvCalculator {

    private val powerItems = mapOf(
        Stats.SPEED to CobblemonItemTags.POWER_ANKLET,
        Stats.SPECIAL_DEFENCE to CobblemonItemTags.POWER_BAND,
        Stats.DEFENCE to CobblemonItemTags.POWER_BELT,
        Stats.ATTACK to CobblemonItemTags.POWER_BRACER,
        Stats.SPECIAL_ATTACK to CobblemonItemTags.POWER_LENS,
        Stats.HP to CobblemonItemTags.POWER_WEIGHT
    )

    override fun calculate(battlePokemon: BattlePokemon, opponentPokemon: BattlePokemon): Map<Stat, Int> {
        val heldItem = battlePokemon.effectedPokemon.heldItemNoCopy()
        val evYield = mutableMapOf<Stat, Int>()

        for ((stat, value) in opponentPokemon.originalPokemon.form.evYield) {
            val boost = if (!heldItem.isEmpty && heldItem.`is`(powerItems[stat])) 8 else 0
            evYield[stat] = evYield.getOrDefault(stat, 0) + value + boost
        }
        return evYield
    }

}