/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.world

import com.cobblemon.mod.common.mixin.StructurePoolAccessor
import com.cobblemon.mod.common.world.structureprocessors.CobblemonStructureProcessorLists
import com.mojang.datafixers.util.Pair
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.ProcessorLists
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.levelgen.structure.pools.LegacySinglePoolElement
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList

object CobblemonStructures {
    private val EMPTY_PROCESSOR_LIST_KEY = ResourceKey.create(Registries.PROCESSOR_LIST, ResourceLocation.fromNamespaceAndPath("minecraft", "empty"))
    private const val pokecenterWeight = 35
    private const val berryFarmWeight = 1
    private const val longPathWeight = 10

    val plainsHousesPoolLocation = ResourceLocation.fromNamespaceAndPath("minecraft", "village/plains/houses")
    val desertHousesPoolLocation = ResourceLocation.fromNamespaceAndPath("minecraft", "village/desert/houses")
    val savannaHousesPoolLocation = ResourceLocation.fromNamespaceAndPath("minecraft", "village/savanna/houses")
    val snowyHousesPoolLocation = ResourceLocation.fromNamespaceAndPath("minecraft", "village/snowy/houses")
    val taigaHousesPoolLocation = ResourceLocation.fromNamespaceAndPath("minecraft", "village/taiga/houses")

    fun registerJigsaws(server: MinecraftServer) {
        val templatePoolRegistry = server.registryAccess().registryOrThrow(Registries.TEMPLATE_POOL)
        val processorListRegistry = server.registryAccess().registryOrThrow(Registries.PROCESSOR_LIST)

        addPokecenters(templatePoolRegistry, processorListRegistry)
        //addLongPaths(templatePoolRegistry, processorListRegistry);
        addBerryFarms(templatePoolRegistry, processorListRegistry)
    }

    fun addBerryFarms(
        templatePoolRegistry: Registry<StructureTemplatePool>,
        processorListRegistry: Registry<StructureProcessorList>
    ) {
        val cropToBerryProcessorList = CobblemonStructureProcessorLists.CROP_TO_BERRY//ResourceKey.create(ResourceKeys.PROCESSOR_LIST, CobblemonProcessorTypes.RANDOM_POOLED_STATES_KEY)

        addBuildingToPool(
            templatePoolRegistry,
            processorListRegistry,
            plainsHousesPoolLocation,
            CobblemonStructureIDs.PLAINS_BERRY_SMALL,
            berryFarmWeight,
            StructureTemplatePool.Projection.RIGID,
            cropToBerryProcessorList
        )

        addBuildingToPool(
            templatePoolRegistry,
            processorListRegistry,
            plainsHousesPoolLocation,
            CobblemonStructureIDs.PLAINS_BERRY_LARGE,
            berryFarmWeight,
            StructureTemplatePool.Projection.RIGID,
            cropToBerryProcessorList
        )

        addBuildingToPool(
            templatePoolRegistry,
            processorListRegistry,
            desertHousesPoolLocation,
            CobblemonStructureIDs.DESERT_BERRY_SMALL,
            berryFarmWeight,
            StructureTemplatePool.Projection.RIGID,
            cropToBerryProcessorList
        )

        addBuildingToPool(
            templatePoolRegistry,
            processorListRegistry,
            desertHousesPoolLocation,
            CobblemonStructureIDs.DESERT_BERRY_LARGE,
            berryFarmWeight,
            StructureTemplatePool.Projection.RIGID,
            cropToBerryProcessorList
        )

        addBuildingToPool(
            templatePoolRegistry,
            processorListRegistry,
            savannaHousesPoolLocation,
            CobblemonStructureIDs.SAVANNA_BERRY_SMALL,
            berryFarmWeight,
            StructureTemplatePool.Projection.RIGID,
            cropToBerryProcessorList
        )

        addBuildingToPool(
            templatePoolRegistry,
            processorListRegistry,
            savannaHousesPoolLocation,
            CobblemonStructureIDs.SAVANNA_BERRY_LARGE,
            berryFarmWeight,
            StructureTemplatePool.Projection.RIGID,
            cropToBerryProcessorList
        )

        addBuildingToPool(
            templatePoolRegistry,
            processorListRegistry,
            taigaHousesPoolLocation,
            CobblemonStructureIDs.TAIGA_BERRY_SMALL,
            berryFarmWeight,
            StructureTemplatePool.Projection.RIGID,
            cropToBerryProcessorList
        )

        addBuildingToPool(
            templatePoolRegistry,
            processorListRegistry,
            taigaHousesPoolLocation,
            CobblemonStructureIDs.TAIGA_BERRY_LARGE,
            berryFarmWeight,
            StructureTemplatePool.Projection.RIGID,
            cropToBerryProcessorList
        )

        addBuildingToPool(
            templatePoolRegistry,
            processorListRegistry,
            snowyHousesPoolLocation,
            CobblemonStructureIDs.SNOWY_BERRY_SMALL,
            berryFarmWeight,
            StructureTemplatePool.Projection.RIGID,
            cropToBerryProcessorList
        )

        addBuildingToPool(
            templatePoolRegistry,
            processorListRegistry,
            snowyHousesPoolLocation,
            CobblemonStructureIDs.SNOWY_BERRY_LARGE,
            berryFarmWeight,
            StructureTemplatePool.Projection.RIGID,
            cropToBerryProcessorList
        )
    }

    private fun addPokecenters(
        templatePoolRegistry: Registry<StructureTemplatePool>,
        processorListRegistry: Registry<StructureProcessorList>
    ) {
        addBuildingToPool(
            templatePoolRegistry,
            processorListRegistry,
            plainsHousesPoolLocation,
            CobblemonStructureIDs.VILLAGE_PLAINS_POKECENTER,
            pokecenterWeight,
            StructureTemplatePool.Projection.RIGID,
            EMPTY_PROCESSOR_LIST_KEY
        )
        addBuildingToPool(
            templatePoolRegistry,
            processorListRegistry,
            desertHousesPoolLocation,
            CobblemonStructureIDs.VILLAGE_DESERT_POKECENTER,
            pokecenterWeight,
            StructureTemplatePool.Projection.RIGID,
            EMPTY_PROCESSOR_LIST_KEY
        )
        addBuildingToPool(
            templatePoolRegistry,
            processorListRegistry,
            savannaHousesPoolLocation,
            CobblemonStructureIDs.VILLAGE_SAVANNA_POKECENTER,
            pokecenterWeight,
            StructureTemplatePool.Projection.RIGID,
            EMPTY_PROCESSOR_LIST_KEY
        )
        addBuildingToPool(
            templatePoolRegistry,
            processorListRegistry,
            snowyHousesPoolLocation,
            CobblemonStructureIDs.VILLAGE_SNOWY_POKECENTER,
            pokecenterWeight,
            StructureTemplatePool.Projection.RIGID,
            EMPTY_PROCESSOR_LIST_KEY
        )
        addBuildingToPool(
            templatePoolRegistry,
            processorListRegistry,
            taigaHousesPoolLocation,
            CobblemonStructureIDs.VILLAGE_TAIGA_POKECENTER,
            pokecenterWeight,
            StructureTemplatePool.Projection.RIGID,
            ProcessorLists.MOSSIFY_10_PERCENT
        )
    }

    private fun addLongPaths(
        templatePoolRegistry: Registry<StructureTemplatePool>,
        processorListRegistry: Registry<StructureProcessorList>
    ) {
        val plainsStreetsPoolLocation = ResourceLocation.parse("minecraft:village/plains/streets")
        val desertStreetsPoolLocation = ResourceLocation.parse("minecraft:village/desert/streets")
        val savannaStreetsPoolLocation = ResourceLocation.parse("minecraft:village/savanna/streets")
        val snowyStreetsPoolLocation = ResourceLocation.parse("minecraft:village/snowy/streets")
        val taigaStreetsPoolLocation = ResourceLocation.parse("minecraft:village/taiga/streets")
        addLegacyBuildingToPool(
            templatePoolRegistry,
            processorListRegistry,
            plainsStreetsPoolLocation,
            CobblemonStructureIDs.PLAINS_LONG_PATH,
            longPathWeight,
            StructureTemplatePool.Projection.TERRAIN_MATCHING,
            ProcessorLists.STREET_PLAINS
        )
        addLegacyBuildingToPool(
            templatePoolRegistry,
            processorListRegistry,
            desertStreetsPoolLocation,
            CobblemonStructureIDs.DESERT_LONG_PATH,
            longPathWeight,
            StructureTemplatePool.Projection.TERRAIN_MATCHING,
            EMPTY_PROCESSOR_LIST_KEY
        )
        addLegacyBuildingToPool(
            templatePoolRegistry,
            processorListRegistry,
            savannaStreetsPoolLocation,
            CobblemonStructureIDs.SAVANNA_LONG_PATH,
            longPathWeight,
            StructureTemplatePool.Projection.TERRAIN_MATCHING,
            ProcessorLists.STREET_SAVANNA
        )
        addLegacyBuildingToPool(
            templatePoolRegistry,
            processorListRegistry,
            snowyStreetsPoolLocation,
            CobblemonStructureIDs.SNOWY_LONG_PATH,
            longPathWeight,
            StructureTemplatePool.Projection.TERRAIN_MATCHING,
            ProcessorLists.STREET_SNOWY_OR_TAIGA
        )
        addLegacyBuildingToPool(
            templatePoolRegistry,
            processorListRegistry,
            taigaStreetsPoolLocation,
            CobblemonStructureIDs.TAIGA_LONG_PATH,
            longPathWeight,
            StructureTemplatePool.Projection.TERRAIN_MATCHING,
            ProcessorLists.STREET_SNOWY_OR_TAIGA
        )
    }

    fun addLegacyBuildingToPool(
        templatePoolRegistry: Registry<StructureTemplatePool>,
        processorListRegistry: Registry<StructureProcessorList>,
        poolRL: ResourceLocation,
        nbtPieceRL: ResourceLocation,
        weight: Int,
        projection: StructureTemplatePool.Projection,
        processorListKey: ResourceKey<StructureProcessorList>
    ) {
        addBuildingToPool(
            templatePoolRegistry,
            processorListRegistry,
            poolRL,
            nbtPieceRL,
            weight,
            projection,
            processorListKey,
            true
        )
    }

    @JvmOverloads
    fun addBuildingToPool(
        templatePoolRegistry: Registry<StructureTemplatePool>,
        processorListRegistry: Registry<StructureProcessorList>,
        poolRL: ResourceLocation,
        nbtPieceRL: ResourceLocation,
        weight: Int,
        projection: StructureTemplatePool.Projection,
        processorListKey: ResourceKey<StructureProcessorList>,
        shouldUseLegacySingePoolElement: Boolean = false
    ) {
        if (processorListRegistry.getHolder(processorListKey).isEmpty) {
            return
        }
        val processorList = processorListRegistry.getHolder(processorListKey).get()
        val pool = templatePoolRegistry[poolRL] as? StructurePoolAccessor ?: return
        val piece = if (shouldUseLegacySingePoolElement) {
            LegacySinglePoolElement.single(nbtPieceRL.toString(), processorList).apply(projection)
        } else {
            SinglePoolElement.single(nbtPieceRL.toString(), processorList).apply(projection)
        }
        repeat(times = weight) { pool.elements.add(piece) }
        val listOfPieceEntries = ArrayList(pool.getElementCounts())
        listOfPieceEntries.add(Pair(piece, weight))
        pool.elements.add(piece)
        pool.setElementCounts(listOfPieceEntries)
    }
}

