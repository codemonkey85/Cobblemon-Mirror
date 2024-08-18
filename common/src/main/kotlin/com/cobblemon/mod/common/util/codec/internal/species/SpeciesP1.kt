/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.codec.internal.species

import com.cobblemon.mod.common.api.abilities.AbilityPool
import com.cobblemon.mod.common.api.pokemon.effect.ShoulderEffect
import com.cobblemon.mod.common.api.pokemon.experience.ExperienceGroup
import com.cobblemon.mod.common.api.pokemon.moves.Learnset
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.util.codec.CodecUtils
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.entity.EntityDimensions
import java.util.Optional

internal data class SpeciesP1(
    val nationalPokedexNumber: Int,
    val baseStats: Map<Stat, Int>,
    val maleRatio: Float,
    val catchRate: Int,
    val baseScale: Float,
    val baseExperienceYield: Int,
    val baseFriendship: Int,
    val evYield: Map<Stat, Int>,
    val experienceGroup: ExperienceGroup,
    val hitbox: EntityDimensions,
    val primaryType: Holder<ElementalType>,
    val secondaryType: Optional<Holder<ElementalType>>,
    val abilityPool: AbilityPool,
    val shoulderMountable: Boolean,
    val shoulderEffects: Set<ShoulderEffect>,
    val learnset: Learnset,
) {
    companion object {
        @JvmStatic
        internal val CODEC: MapCodec<SpeciesP1> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("nationalPokedexNumber").forGetter(SpeciesP1::nationalPokedexNumber),
                Codec.unboundedMap(Stat.PERMANENT_ONLY_CODEC, ExtraCodecs.POSITIVE_INT).fieldOf("baseStats").forGetter(SpeciesP1::baseStats),
                Codec.FLOAT.fieldOf("maleRatio").forGetter(SpeciesP1::maleRatio),
                ExtraCodecs.POSITIVE_INT.fieldOf("catchRate").forGetter(SpeciesP1::catchRate),
                Codec.FLOAT.optionalFieldOf("baseScale", 1F).forGetter(SpeciesP1::baseScale),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("baseExperienceYield").forGetter(SpeciesP1::baseExperienceYield),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("baseFriendship").forGetter(SpeciesP1::baseFriendship),
                Codec.unboundedMap(Stat.PERMANENT_ONLY_CODEC, ExtraCodecs.POSITIVE_INT).fieldOf("evYield").forGetter(SpeciesP1::evYield),
                ExperienceGroup.CODEC.fieldOf("experienceGroup").forGetter(SpeciesP1::experienceGroup),
                CodecUtils.ENTITY_DIMENSION.fieldOf("hitbox").forGetter(SpeciesP1::hitbox),
                ElementalType.CODEC.fieldOf("primaryType").forGetter(SpeciesP1::primaryType),
                ElementalType.CODEC.optionalFieldOf("secondaryType").forGetter(SpeciesP1::secondaryType),
                AbilityPool.CODEC.fieldOf("abilities").forGetter(SpeciesP1::abilityPool),
                Codec.BOOL.fieldOf("shoulderMountable").forGetter(SpeciesP1::shoulderMountable),
                CodecUtils.setOf(ShoulderEffect.CODEC).fieldOf("shoulderEffects").forGetter(SpeciesP1::shoulderEffects),
                Learnset.CODEC.fieldOf("moves").forGetter(SpeciesP1::learnset),
            ).apply(instance, ::SpeciesP1)
        }

        internal fun from(species: Species): SpeciesP1 = SpeciesP1(
            species.nationalPokedexNumber,
            species.baseStats,
            species.maleRatio,
            species.catchRate,
            species.baseScale,
            species.baseExperienceYield,
            species.baseFriendship,
            species.evYield,
            species.experienceGroup,
            species.hitbox,
            species.primaryTypeHolder,
            species.secondaryTypeHolder,
            species.abilities,
            species.shoulderMountable,
            species.shoulderEffects,
            species.moves
        )
    }
}