/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.experience

import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.pokemon.evolution.requirements.LevelRequirement
import kotlin.math.pow

interface ExperienceCalculator {
    fun calculate(battlePokemon: BattlePokemon) = battlePokemon.facedOpponents /* Change later to be all opponents if exp share held */
        .filter { it.health == 0 }
        .sumOf { calculate(battlePokemon, it) }

    fun calculate(battlePokemon: BattlePokemon, opponentPokemon: BattlePokemon): Int
}

object StandardExperienceCalculator : ExperienceCalculator {
    override fun calculate(battlePokemon: BattlePokemon, opponentPokemon: BattlePokemon): Int {
        val trainerMultiplier = if (opponentPokemon.effectedPokemon.isWild()) 1.0 else 1.5 /* Not used in modern formulas */
        val baseExp = opponentPokemon.originalPokemon.form.baseExperienceYield
        val luckyEggMultiplier = 1.0
        val affectionMultiplier = 1.0
        val level = opponentPokemon.originalPokemon.level
        val levelVictor = battlePokemon.originalPokemon.level
        val pointPowerMultiplier = 1.0
        val participatedMultiplier = if (opponentPokemon in battlePokemon.facedOpponents) 1.0 else 2.0 // Implement after Exp Share etc
        val originalTrainerMultiplier = 1.0
        val evolutionMultiplier =
            if (battlePokemon.effectedPokemon.evolutionProxy.server().any { evolution ->
                val requirements = evolution.requirements.asSequence()
                requirements.any { it is LevelRequirement } && requirements.all { it.check(battlePokemon.effectedPokemon) }
            }) 1.2 else 1.0
        val term1 = (baseExp * level * affectionMultiplier * evolutionMultiplier) / (5 * participatedMultiplier)
        val term2 = ((2.0 * level + 10) / (level + levelVictor + 10)).pow(2.5)
        val term3 = originalTrainerMultiplier * luckyEggMultiplier * pointPowerMultiplier

        return (term1 * term2 * term3).toInt()
    }
}