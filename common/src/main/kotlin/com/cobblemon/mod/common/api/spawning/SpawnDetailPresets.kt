/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.spawning.condition.SpawningCondition
import com.cobblemon.mod.common.api.spawning.context.RegisteredSpawningContext
import com.cobblemon.mod.common.api.spawning.detail.PossibleHeldItem
import com.cobblemon.mod.common.api.spawning.preset.SpawnDetailPreset
import com.cobblemon.mod.common.util.adapters.*
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mojang.datafixers.util.Either
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.Block
import net.minecraft.tags.TagKey
import net.minecraft.server.packs.PackType
import net.minecraft.server.level.ServerPlayer
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.material.Fluid

/**
 * Data registry for [SpawnDetailPreset]s. These help the maintainability of spawn files by allowing common presets
 * to be defined separately to the spawns that obey it. You can register custom presets either programmatically or
 * by adding preset JSONs to the spawn_detail_presets data folder.
 *
 * @author Hiroku
 * @since December 9th, 2022
 */
object SpawnDetailPresets : JsonDataRegistry<SpawnDetailPreset> {
    val GSON = GsonBuilder()
        .setPrettyPrinting()
        .setLenient()
        .disableHtmlEscaping()
        .registerTypeAdapter(SpawnBucket::class.java, SpawnBucketAdapter)
        .registerTypeAdapter(RegisteredSpawningContext::class.java, RegisteredSpawningContextAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(RegistryLikeCondition::class.java, Biome::class.java).type, BiomeLikeConditionAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(RegistryLikeCondition::class.java, Block::class.java).type, BlockLikeConditionAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(RegistryLikeCondition::class.java, Fluid::class.java).type, FluidLikeConditionAdapter)
        .registerTypeAdapter(
            TypeToken.getParameterized(
                Either::class.java,
                ResourceLocation::class.java,
                TypeToken.getParameterized(
                    TagKey::class.java,
                    Structure::class.java
                ).type
            ).type,
            EitherIdentifierOrTagAdapter(Registries.STRUCTURE)
        )
        .registerTypeAdapter(SpawnDetailPreset::class.java, SpawnDetailPresetAdapter)
        .registerTypeAdapter(ResourceLocation::class.java, IdentifierAdapter)
        .registerTypeAdapter(SpawningCondition::class.java, SpawningConditionAdapter)
        .registerTypeAdapter(TimeRange::class.java, IntRangesAdapter(TimeRange.timeRanges) { TimeRange(*it) })
        .registerTypeAdapter(MoonPhaseRange::class.java, IntRangesAdapter(MoonPhaseRange.moonPhaseRanges) { MoonPhaseRange(*it) })
        .registerTypeAdapter(PokemonProperties::class.java, pokemonPropertiesShortAdapter)
        .registerTypeAdapter(PossibleHeldItem::class.java, PossibleHeldItemAdapter)
        .create()

    val presetTypes = mutableMapOf<String, Class<out SpawnDetailPreset>>()
    fun <T : SpawnDetailPreset> registerPresetType(name: String, detailClass: Class<T>) {
        presetTypes[name] = detailClass
    }

    override val gson: Gson = GSON
    override val typeToken = TypeToken.get(SpawnDetailPreset::class.java)
    override val resourcePath = "spawn_detail_presets"
    override val id = cobblemonResource(resourcePath)
    override val type = PackType.SERVER_DATA
    override val observable = SimpleObservable<SpawnDetailPresets>()

    var presets = mutableMapOf<ResourceLocation, SpawnDetailPreset>()

    override fun sync(player: ServerPlayer) {}
    override fun reload(data: Map<ResourceLocation, SpawnDetailPreset>) {
        this.presets = data.toMutableMap()
        Cobblemon.LOGGER.info("Loaded ${presets.size} spawn detail presets.")
    }
}