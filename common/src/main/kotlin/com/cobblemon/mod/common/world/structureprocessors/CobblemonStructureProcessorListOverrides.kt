/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.world.structureprocessors

import net.minecraft.core.registries.Registries
import net.minecraft.server.MinecraftServer

object CobblemonStructureProcessorListOverrides {
    val ResourceKey = Registries.PROCESSOR_LIST

    fun register(server: MinecraftServer) {
        //val registry = server.registryManager.get(ResourceKey)
        // registerFarmOverrides(registry)
    }

//    fun registerFarmOverrides(registry: Registry<StructureProcessorList>) {
//        val desertFarm = registry[StructureProcessorLists.FARM_DESERT] as StructureProcessorListBridge
//        val plainsFarm = registry[StructureProcessorLists.FARM_PLAINS] as StructureProcessorListBridge
//        val taigaFarm = registry[StructureProcessorLists.FARM_TAIGA] as StructureProcessorListBridge
//        val savannahFarm = registry[StructureProcessorLists.FARM_SAVANNA] as StructureProcessorListBridge
//        val snowyFarm = registry[StructureProcessorLists.FARM_SNOWY] as StructureProcessorListBridge
//
//        val naturalBerries = BerryHelper.getNaturallyGeneratingBerries().map {
//            it.defaultState.with(BerryBlock.AGE, 5).with(BerryBlock.WAS_GENERATED, true)
//        }
//
//        val processor = RandomizedStructureMappedBlockStatePairProcessor(
//            naturalBerries,
//            3,
//            listOf(TagMatchRuleTest(BlockTags.CROPS)),
//            0.5f
//        )
//
//        desertFarm.append(processor)
//        plainsFarm.append(processor)
//        taigaFarm.append(processor)
//        savannahFarm.append(processor)
//        snowyFarm.append(processor)
//    }

}
