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
import com.cobblemon.mod.common.util.codec.CodecUtils
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder

/**
 * An [EvolutionRequirement] that succeeds when any of the [possibilities] are valid.
 *
 * @property possibilities A collection of possible [EvolutionRequirement]s that can allow this requirement to be valid.
 */
class AnyRequirement(val possibilities: Set<EvolutionRequirement>) : EvolutionRequirement {

    private constructor(possibilities: Collection<EvolutionRequirement>) : this(possibilities.toSet())

    override val type: EvolutionRequirementType<*> = TODO("EvolutionRequirementType.ANY")

    override fun check(pokemon: Pokemon) = this.possibilities.any { it.check(pokemon) }

    companion object {

        @JvmStatic
        val CODEC: MapCodec<AnyRequirement> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.list(EvolutionRequirement.CODEC, 0, Int.MAX_VALUE)
                    .fieldOf("possibilities")
                    .forGetter { it.possibilities.toList() }
            ).apply(instance, ::AnyRequirement)
        }

    }
}