/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.requirements

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirement
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirementType
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.evolution.predicate.NbtItemPredicate
import com.cobblemon.mod.common.util.codec.CodecUtils
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import kotlin.math.max
import kotlin.math.min

class LevelRequirement(minLevel: Int, maxLevel: Int) : EvolutionRequirement {

    val minLevel = min(minLevel, maxLevel)

    val maxLevel = max(minLevel, maxLevel)

    override fun check(pokemon: Pokemon) = pokemon.level in minLevel..maxLevel

    override val type: EvolutionRequirementType<*> = EvolutionRequirementType.LEVEL

    companion object {
        @JvmStatic
        val CODEC: MapCodec<LevelRequirement> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                CodecUtils.dynamicIntRange(1) { Cobblemon.config.maxPokemonLevel }.fieldOf("minLevel").forGetter(LevelRequirement::minLevel),
                CodecUtils.dynamicIntRange(1) { Cobblemon.config.maxPokemonLevel }.fieldOf("maxLevel").forGetter(LevelRequirement::maxLevel),
            ).apply(instance, ::LevelRequirement)
        }
    }
}