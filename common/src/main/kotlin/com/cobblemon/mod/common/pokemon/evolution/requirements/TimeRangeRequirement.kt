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
import com.cobblemon.mod.common.api.spawning.TimeRange
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.evolution.requirements.template.EntityQueryRequirement
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.entity.LivingEntity

/**
 * An [EvolutionRequirement] for when the current time must be in the provided [TimeRange].
 *
 * @property range The required [TimeRange],
 * @author Licious
 * @since March 26th, 2022
 */
class TimeRangeRequirement(val range: TimeRange) : EntityQueryRequirement {

    override fun check(pokemon: Pokemon, queriedEntity: LivingEntity) = this.range.contains((queriedEntity.level().dayTime() % DAY_DURATION).toInt())

    override val type: EvolutionRequirementType<*> = EvolutionRequirementType.TIME_RANGE

    companion object {

        private const val DAY_DURATION = 24000

        @JvmStatic
        val CODEC: MapCodec<TimeRangeRequirement> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                TimeRange.CODEC.fieldOf("range").forGetter(TimeRangeRequirement::range),
            ).apply(instance, ::TimeRangeRequirement)
        }

    }

}