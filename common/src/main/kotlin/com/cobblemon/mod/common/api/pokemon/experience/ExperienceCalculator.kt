/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.experience

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.tags.CobblemonItemTags
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.pokemon.OriginalTrainerType
import com.cobblemon.mod.common.pokemon.evolution.requirements.LevelRequirement
import kotlin.math.pow
import kotlin.math.roundToInt

fun interface ExperienceCalculator {
    fun calculate(battlePokemon: BattlePokemon, opponentPokemon: BattlePokemon, participationMultiplier: Double): Int
}

object StandardExperienceCalculator : ExperienceCalculator {
    override fun calculate(battlePokemon: BattlePokemon, opponentPokemon: BattlePokemon, participationMultiplier: Double): Int {
        val baseExp = opponentPokemon.originalPokemon.form.baseExperienceYield
        val opponentLevel = opponentPokemon.effectedPokemon.level
        val term1 = (baseExp * opponentLevel) / 5.0
        // This is meant to be a division but this is due to the intended behavior of handling the 2.0 sent over from Exp. All in modern Pokémon
        val term2 = 1 * participationMultiplier
        val victorPokemon = battlePokemon.effectedPokemon
        val victorLevel = victorPokemon.level
        val term3 = (((2.0 * opponentLevel) + 10) / (opponentLevel + victorLevel + 10)).pow(2.5)
        // ToDo 1.7 if pokemon was traded with someone of different locale/language
        val nonOtBonus = if (battlePokemon.effectedPokemon.originalTrainerType == OriginalTrainerType.PLAYER &&
                battlePokemon.effectedPokemon.originalTrainer.equals(battlePokemon.actor.uuid.toString())) 1.0 else 1.5
        val luckyEggMultiplier = if (battlePokemon.effectedPokemon.heldItemNoCopy().`is`(CobblemonItemTags.LUCKY_EGG)) Cobblemon.config.luckyEggMultiplier else 1.0
        val evolutionMultiplier = if (battlePokemon.effectedPokemon.evolutionProxy.server().any { evolution ->
            val requirements = evolution.requirements.asSequence()
            requirements.any { it is LevelRequirement } && requirements.all { it.check(battlePokemon.effectedPokemon) }
        }) 1.2 else 1.0
        val affectionMultiplier = if (battlePokemon.effectedPokemon.friendship >= 220) 1.2 else 1.0
        // that's us!
        val gimmickBoost = Cobblemon.config.experienceMultiplier
        val term4 = term1 * term2 * term3 + 1
        return (term4 * nonOtBonus * luckyEggMultiplier * evolutionMultiplier * affectionMultiplier * gimmickBoost).roundToInt()
    }
}