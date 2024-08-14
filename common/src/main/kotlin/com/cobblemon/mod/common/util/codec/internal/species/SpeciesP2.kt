/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.codec.internal.species

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.pokemon.ai.PokemonBehaviour
import com.cobblemon.mod.common.pokemon.lighthing.LightingData
import com.cobblemon.mod.common.registry.CobblemonRegistries
import com.cobblemon.mod.common.util.codec.CodecUtils
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.ExtraCodecs
import java.util.*

internal data class SpeciesP2(
    val standingEyeHeight: Optional<Float>,
    val swimmingEyeHeight: Optional<Float>,
    val flyingEyeHeight: Optional<Float>,
    val behaviour: PokemonBehaviour,
    val eggCycles: Int,
    val eggGroups: Set<EggGroup>,
    val dynamaxBlocked: Boolean,
    val implemented: Boolean,
    val height: Float,
    val weight: Float,
    val preEvolution: Optional<Holder<Species>>,
    val battleTheme: ResourceLocation,
    val lightingData: Optional<LightingData>,
) {
    companion object {
        @JvmStatic
        internal val CODEC: MapCodec<SpeciesP2> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.FLOAT.optionalFieldOf("standingEyeHeight").forGetter(SpeciesP2::standingEyeHeight),
                Codec.FLOAT.optionalFieldOf("swimmingEyeHeight").forGetter(SpeciesP2::swimmingEyeHeight),
                Codec.FLOAT.optionalFieldOf("flyingEyeHeight").forGetter(SpeciesP2::flyingEyeHeight),
                PokemonBehaviour.CODEC.optionalFieldOf("behaviour", PokemonBehaviour()).forGetter(SpeciesP2::behaviour),
                ExtraCodecs.POSITIVE_INT.fieldOf("eggCycles").forGetter(SpeciesP2::eggCycles),
                CodecUtils.setOf(EggGroup.CODEC).fieldOf("eggGroups").forGetter(SpeciesP2::eggGroups),
                Codec.BOOL.fieldOf("dynamaxBlocked").forGetter(SpeciesP2::dynamaxBlocked),
                Codec.BOOL.optionalFieldOf("implemented", true).forGetter(SpeciesP2::implemented),
                Codec.FLOAT.fieldOf("height").forGetter(SpeciesP2::height),
                Codec.FLOAT.fieldOf("weight").forGetter(SpeciesP2::weight),
                CobblemonRegistries.SPECIES.holderByNameCodec().optionalFieldOf("preEvolution").forGetter(SpeciesP2::preEvolution),
                ResourceLocation.CODEC.optionalFieldOf("battleTheme", CobblemonSounds.PVW_BATTLE.location).forGetter(SpeciesP2::battleTheme),
                LightingData.CODEC.optionalFieldOf("lightingData").forGetter(SpeciesP2::lightingData),
            ).apply(instance, ::SpeciesP2)
        }

        internal fun from(species: Species): SpeciesP2 = SpeciesP2(
            Optional.ofNullable(species.standingEyeHeight),
            Optional.ofNullable(species.swimmingEyeHeight),
            Optional.ofNullable(species.flyingEyeHeight),
            species.behaviour,
            species.eggCycles,
            species.eggGroups,
            species.dynamaxBlocked,
            species.implemented,
            species.height,
            species.weight,
            Optional.ofNullable(species.preEvolution),
            species.battleTheme,
            Optional.ofNullable(species.lightingData),
        )
    }
}