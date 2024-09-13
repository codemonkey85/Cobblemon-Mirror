/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.requirements

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirement
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirementType
import com.cobblemon.mod.common.pokemon.Pokemon
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder

/**
 * An [EvolutionRequirement] for when the [Pokemon] must match [PokemonProperties.matches].
 *
 * @property target The matcher for this requirement.
 * @author Licious
 * @since March 26th, 2022
 */
class PokemonPropertiesRequirement(val target: PokemonProperties) : EvolutionRequirement {

    override fun check(pokemon: Pokemon) = this.target.matches(pokemon)

    override val type: EvolutionRequirementType<*> = EvolutionRequirementType.PROPERTIES

    companion object {
        @JvmStatic
        val CODEC: MapCodec<PokemonPropertiesRequirement> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                PokemonProperties.CODEC.fieldOf("target").forGetter(PokemonPropertiesRequirement::target),
            ).apply(instance, ::PokemonPropertiesRequirement)
        }
    }
}