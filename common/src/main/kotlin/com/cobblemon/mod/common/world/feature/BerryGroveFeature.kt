/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.world.feature

import com.cobblemon.mod.common.api.berry.BerryHelper
import com.cobblemon.mod.common.api.tags.CobblemonBlockTags
import com.cobblemon.mod.common.block.BerryBlock
import com.cobblemon.mod.common.util.weightedSelection
import net.minecraft.core.Vec3i
import net.minecraft.data.worldgen.placement.PlacementUtils
import net.minecraft.util.valueproviders.ClampedNormalInt
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.GrassBlock
import net.minecraft.world.level.chunk.status.ChunkStatus
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
import net.minecraft.world.level.levelgen.feature.stateproviders.RandomizedIntStateProvider
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter

class BerryGroveFeature : Feature<NoneFeatureConfiguration>(NoneFeatureConfiguration.CODEC){
    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val worldGenLevel: WorldGenLevel = context.level()
        val random = context.random()
        val origin = context.origin()

        val isGenerating = worldGenLevel.getChunk(origin).persistedStatus != ChunkStatus.FULL

        if (!isGenerating) return false
        val biome = worldGenLevel.getBiome(origin)
        //This basically goes through and finds the berries whose preferred biome we are in
        //Maybe cache these per biome in a map?
        val validTrees = BerryHelper.getBerriesForBiome(biome)
        if (validTrees.isEmpty()) return false
        val pickedTree = validTrees.weightedSelection { it.berry()!!.weight }!!
        val berry = pickedTree.berry()!!
        val numTreesToGen = pickedTree.berry()?.spawnConditions?.sumOf { cond ->
            (cond.getGroveSize(random).takeIf { cond.canSpawn(berry, biome) } ) ?: 0
        } ?: 0
        var numTreesLeftToGen = numTreesToGen
        val defTreeState = BlockStateProvider.simple(pickedTree.defaultBlockState().setValue(BerryBlock.WAS_GENERATED, true))

        val randomTreeStateProvider = RandomizedIntStateProvider(
            defTreeState, BerryBlock.AGE,
            ClampedNormalInt.of(4f, 1f, 3, 5))
        val blockPlaceFeature = PlacementUtils.inlinePlaced(
            SIMPLE_BLOCK,
            SimpleBlockConfiguration(randomTreeStateProvider),
            BlockPredicateFilter.forPredicate(BlockPredicate.matchesTag(CobblemonBlockTags.BERRY_REPLACEABLE)),
            BlockPredicateFilter.forPredicate(BlockPredicate.matchesTag(Vec3i(0, -1, 0), CobblemonBlockTags.BERRY_WILD_SOIL))
        ).value()
        val possiblePositions = listOf(
            origin.north(),
            origin.north().east(),
            origin.east(),
            origin.south().east(),
            origin.south(),
            origin.south().west(),
            origin.west(),
            origin.north().west(),
            origin.above().north(),
            origin.above().north().east(),
            origin.above().east(),
            origin.above().south().east(),
            origin.above().south(),
            origin.above().south().west(),
            origin.above().west(),
            origin.above().north().west(),
            origin.below().north(),
            origin.below().north().east(),
            origin.below().east(),
            origin.below().south().east(),
            origin.below().south(),
            origin.below().south().west(),
            origin.below().west(),
            origin.below().north().west(),
        ).shuffled()
        for (dir in possiblePositions) {
            if (numTreesLeftToGen > 0) {
                if (blockPlaceFeature?.place(worldGenLevel, context.chunkGenerator(), random, dir) == true) {
                    worldGenLevel.blockUpdated(dir, worldGenLevel.getBlockState(dir).block)
                    numTreesLeftToGen--
                    val below = worldGenLevel.getBlockState(dir.below())
                    if (below.`is`(Blocks.GRASS_BLOCK) && below.getValue(GrassBlock.SNOWY)) {
                        worldGenLevel.setBlock(dir.below(), below.setValue(GrassBlock.SNOWY, false), 2)
                    }
                    worldGenLevel.setBlock(dir.above(), Blocks.AIR.defaultBlockState(), 2)
                }
            }
        }
        return numTreesToGen != numTreesLeftToGen
    }

}
