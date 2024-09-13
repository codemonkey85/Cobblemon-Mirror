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
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.pokemon.Pokemon
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.StringRepresentable

class StatRatioRequirement(val stat1: Stat, val stat2: Stat, val comparison: Comparison) : EvolutionRequirement {

    override val type: EvolutionRequirementType<*> = EvolutionRequirementType.STAT_RATIO

    override fun check(pokemon: Pokemon): Boolean {
        try {
            val value1 = pokemon.getStat(stat1)
            val value2 = pokemon.getStat(stat2)
            return when (comparison) {
                Comparison.LOWER -> value1 < value2
                Comparison.HIGHER -> value1 > value2
                Comparison.EQUAL -> value1 == value2
            }
        } catch (ignore: Exception) {
            // Some stat impl may not accept a certain stat type, we by default only accept battle ones for example.
            return false
        }
    }

    enum class Comparison : StringRepresentable {

        EQUAL,
        LOWER,
        HIGHER;

        override fun getSerializedName(): String = this.name

        companion object {

            @JvmStatic
            val CODEC = StringRepresentable.fromEnum(Comparison::values)
        }

    }

    companion object {

        @JvmStatic
        val CODEC: MapCodec<StatRatioRequirement> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Stat.ALL_CODEC.fieldOf("stat1").forGetter(StatRatioRequirement::stat1),
                Stat.ALL_CODEC.fieldOf("stat2").forGetter(StatRatioRequirement::stat2),
                Comparison.CODEC.fieldOf("comparison").forGetter(StatRatioRequirement::comparison),
            ).apply(instance, ::StatRatioRequirement)
        }

    }
}