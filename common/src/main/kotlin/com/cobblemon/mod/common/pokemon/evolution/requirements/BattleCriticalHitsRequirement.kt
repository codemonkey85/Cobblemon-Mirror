/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.requirements

import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirement
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirementType
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.evolution.progress.LastBattleCriticalHitsEvolutionProgress
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.ExtraCodecs

/**
 * An [EvolutionRequirement] for a certain amount of critical hits in a single battle.
 *
 * @param amount The amount of critical hits required.
 *
 * @author Licious
 * @since October 2nd, 2022
 */
class BattleCriticalHitsRequirement(val amount: Int) : EvolutionRequirement {

    override fun check(pokemon: Pokemon): Boolean = pokemon.evolutionProxy.current()
        .progress()
        .filterIsInstance<LastBattleCriticalHitsEvolutionProgress>()
        .any { progress -> progress.currentProgress().amount >= this.amount }

    override val type: EvolutionRequirementType<*> = EvolutionRequirementType.BATTLE_CRITICAL_HITS

    companion object {
        @JvmStatic
        val CODEC: MapCodec<BattleCriticalHitsRequirement> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(BattleCriticalHitsRequirement::amount)
            ).apply(instance, ::BattleCriticalHitsRequirement)
        }
    }

}