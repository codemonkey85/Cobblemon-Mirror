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
import com.cobblemon.mod.common.api.pokemon.feature.IntSpeciesFeature
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.codec.CodecUtils
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder

class PropertyRangeRequirement(val range: IntRange, val featureKey: String) : EvolutionRequirement {

    override fun check(pokemon: Pokemon): Boolean {
        val feature: IntSpeciesFeature = pokemon.getFeature(featureKey) ?: return false
        return this.range.contains(feature.value)
    }

    override val type: EvolutionRequirementType<*> = EvolutionRequirementType.PROPERTY_RANGE

    companion object {
        @JvmStatic
        val CODEC: MapCodec<PropertyRangeRequirement> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                CodecUtils.intRange(Codec.INT).fieldOf("range").forGetter(PropertyRangeRequirement::range),
                Codec.STRING.fieldOf("feature").forGetter(PropertyRangeRequirement::featureKey),
            ).apply(instance, ::PropertyRangeRequirement)
        }
    }
}