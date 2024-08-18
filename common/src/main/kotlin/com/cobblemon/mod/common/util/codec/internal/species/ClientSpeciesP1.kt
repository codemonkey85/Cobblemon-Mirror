/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.codec.internal.species

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.pokemon.experience.ExperienceGroup
import com.cobblemon.mod.common.api.pokemon.moves.Learnset
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.pokemon.lighthing.LightingData
import com.cobblemon.mod.common.registry.CobblemonRegistries
import com.cobblemon.mod.common.util.codec.CodecUtils
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.entity.EntityDimensions
import java.util.*

internal data class ClientSpeciesP1(
    val nationalPokedexNumber: Int,
    val baseStats: Map<Stat, Int>,
    val baseScale: Float,
    val height: Float,
    val weight: Float,
    val experienceGroup: ExperienceGroup,
    val hitbox: EntityDimensions,
    val primaryType: Holder<ElementalType>,
    val secondaryType: Optional<Holder<ElementalType>>,
    val learnset: Learnset,
    val battleTheme: ResourceLocation,
    val lightingData: Optional<LightingData>,
    val implemented: Boolean,
) {

    companion object {
        @JvmStatic
        internal val CODEC: MapCodec<ClientSpeciesP1> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("nationalPokedexNumber").forGetter(ClientSpeciesP1::nationalPokedexNumber),
                Codec.unboundedMap(Stat.PERMANENT_ONLY_CODEC, ExtraCodecs.POSITIVE_INT).fieldOf("baseStats").forGetter(ClientSpeciesP1::baseStats),
                Codec.FLOAT.optionalFieldOf("baseScale", 1F).forGetter(ClientSpeciesP1::baseScale),
                Codec.FLOAT.fieldOf("height").forGetter(ClientSpeciesP1::height),
                Codec.FLOAT.fieldOf("weight").forGetter(ClientSpeciesP1::weight),
                ExperienceGroup.CODEC.fieldOf("experienceGroup").forGetter(ClientSpeciesP1::experienceGroup),
                CodecUtils.ENTITY_DIMENSION.fieldOf("hitbox").forGetter(ClientSpeciesP1::hitbox),
                ElementalType.CODEC.fieldOf("primaryType").forGetter(ClientSpeciesP1::primaryType),
                ElementalType.CODEC.optionalFieldOf("secondaryType").forGetter(ClientSpeciesP1::secondaryType),
                Learnset.CLIENT_CODEC.fieldOf("moves").forGetter(ClientSpeciesP1::learnset),
                ResourceLocation.CODEC.optionalFieldOf("battleTheme", CobblemonSounds.PVW_BATTLE.location).forGetter(ClientSpeciesP1::battleTheme),
                LightingData.CODEC.optionalFieldOf("lightingData").forGetter(ClientSpeciesP1::lightingData),
                Codec.BOOL.optionalFieldOf("implemented", true).forGetter(ClientSpeciesP1::implemented)
            ).apply(instance, ::ClientSpeciesP1)
        }

        internal fun from(species: Species): ClientSpeciesP1 = ClientSpeciesP1(
            species.nationalPokedexNumber,
            species.baseStats,
            species.baseScale,
            species.height,
            species.weight,
            species.experienceGroup,
            species.hitbox,
            species.primaryTypeHolder,
            species.secondaryTypeHolder,
            species.moves,
            species.battleTheme,
            Optional.ofNullable(species.lightingData),
            species.implemented,
        )
    }
}