/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.requirements

import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirementType
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.evolution.requirements.template.EntityQueryRequirement
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.StringRepresentable
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import java.util.Optional

class WeatherRequirement(
    val state: WeatherState
) : EntityQueryRequirement {

    enum class WeatherState(private val checker: (level: Level) -> Boolean) : StringRepresentable {

        CLEAR({ level -> !level.isRaining && !level.isThundering }),
        RAINING({ level -> level.isRaining }),
        THUNDERING({ level -> level.isThundering });

        override fun getSerializedName(): String = this.name

        fun fits(level: Level): Boolean = this.checker(level)

        companion object {

            @JvmStatic
            val CODEC = StringRepresentable.fromEnum(WeatherState::values)

        }

    }

    override fun check(pokemon: Pokemon, queriedEntity: LivingEntity): Boolean = this.state.fits(queriedEntity.level())

    override val type: EvolutionRequirementType<*> = EvolutionRequirementType.WEATHER

    companion object {

        @JvmStatic
        val CODEC: MapCodec<WeatherRequirement> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                WeatherState.CODEC.fieldOf("state").forGetter(WeatherRequirement::state),
            ).apply(instance, ::WeatherRequirement)
        }

    }

}