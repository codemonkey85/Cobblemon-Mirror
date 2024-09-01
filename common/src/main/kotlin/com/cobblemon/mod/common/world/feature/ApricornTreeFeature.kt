/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.world.feature

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.api.tags.CobblemonBiomeTags
import com.cobblemon.mod.common.block.ApricornBlock
import com.cobblemon.mod.common.util.randomNoCopy
import com.google.common.collect.Lists
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Direction.*
import net.minecraft.tags.BlockTags
import net.minecraft.util.RandomSource
import net.minecraft.world.level.LevelSimulatedReader
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.status.ChunkStatus
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.TreeFeature
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration
import kotlin.random.Random.Default.nextInt

class ApricornTreeFeature : Feature<BlockStateConfiguration>(BlockStateConfiguration.CODEC) {

    override fun place(context: FeaturePlaceContext<BlockStateConfiguration>) : Boolean {
        val worldGenLevel: WorldGenLevel = context.level()
        val random = context.random()
        val origin = context.origin()

        val isGenerating = worldGenLevel.getChunk(origin).persistedStatus != ChunkStatus.FULL

        if (isGenerating) {
            val biome = worldGenLevel.getBiome(origin)
            val multiplier = if (biome.`is`(CobblemonBiomeTags.HAS_APRICORNS_SPARSE)) {
                0.1F
            } else if (biome.`is`(CobblemonBiomeTags.HAS_APRICORNS_DENSE)) {
                10F
            } else if (biome.`is`(CobblemonBiomeTags.HAS_APRICORNS_NORMAL)) {
                1.0F
            } else {
                return false
            }

            if (random.nextFloat() > multiplier * Cobblemon.config.baseApricornTreeGenerationChance) {
                return false
            }
        }

        if (!worldGenLevel.getBlockState(origin.below()).`is`(BlockTags.DIRT)) {
            return false
        }

        // Create trunk
        val logState = CobblemonBlocks.APRICORN_LOG.defaultBlockState()
        for (y in 0..4) {
            try {
                val logPos = origin.relative(UP, y)
                worldGenLevel.setBlock(logPos, logState, 2)
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }

        // Decorate with leaves
        val allApricornSpots: MutableList<List<Pair<Direction, BlockPos>>> = mutableListOf()
        val leafBlock = CobblemonBlocks.APRICORN_LEAVES.defaultBlockState()

        val layerOnePos = origin.relative(UP)
        for (direction in listOf(NORTH, EAST, SOUTH, WEST)) {
            var leafPos = layerOnePos.relative(direction)
            setBlockIfClear(worldGenLevel, leafPos, LeavesBlock.updateDistance(leafBlock, worldGenLevel, leafPos))
            for (offset in 1..3) {
                leafPos = leafPos.above()
                setBlockIfClear(worldGenLevel, leafPos, LeavesBlock.updateDistance(leafBlock, worldGenLevel, leafPos))
            }
        }

        val layerOneExtenders = getLayerOneVariation(layerOnePos, random)
        setBlockIfClear(worldGenLevel, layerOneExtenders.first, LeavesBlock.updateDistance(leafBlock, worldGenLevel, layerOneExtenders.first))
        setBlockIfClear(worldGenLevel, layerOneExtenders.second, LeavesBlock.updateDistance(leafBlock, worldGenLevel, layerOneExtenders.second))

        for (coords in listOf(Pair(1, 1), Pair(-1, -1), Pair(1, -1), Pair(-1, 1))) {
            var leafPos = layerOnePos.offset(coords.first, 0, coords.second)
            setBlockIfClear(worldGenLevel, leafPos, LeavesBlock.updateDistance(leafBlock, worldGenLevel, leafPos))
            for (offset in 1..3) {
                leafPos = leafPos.above()
                setBlockIfClear(worldGenLevel, leafPos, LeavesBlock.updateDistance(leafBlock, worldGenLevel, leafPos))
            }
        }

        val layerTwoPos = origin.offset(0, 2, 0)
        for (direction in Lists.newArrayList(NORTH, EAST, SOUTH, WEST)) {
            val apricornSpots = mutableListOf<Pair<Direction, BlockPos>>()
            var leafPos = layerTwoPos.offset(direction.stepX * 2, direction.stepY * 2, direction.stepZ * 2)

            setBlockIfClear(worldGenLevel, leafPos, LeavesBlock.updateDistance(leafBlock, worldGenLevel, leafPos))
            apricornSpots.add(direction.opposite to leafPos.relative(direction))

            leafPos = leafPos.above()
            setBlockIfClear(worldGenLevel, leafPos, LeavesBlock.updateDistance(leafBlock, worldGenLevel, leafPos))
            apricornSpots.add(direction.opposite to leafPos.relative(direction))

            allApricornSpots.add(apricornSpots)
        }

        for (coords in Lists.newArrayList(Pair(1, 2), Pair(-1, 2), Pair(1, -2), Pair(-2, 1), Pair(2, 1), Pair(-2, -1), Pair(-1, -2), Pair(2, -1))) {
            val apricornSpots = mutableListOf<Pair<Direction, BlockPos>>()
            var leafPos = layerTwoPos.offset(coords.first, 0, coords.second)
            setBlockIfClear(worldGenLevel, leafPos, LeavesBlock.updateDistance(leafBlock, worldGenLevel, leafPos))

            for (direction in listOf(NORTH, EAST, SOUTH, WEST)) {
                val apricornPos = leafPos.relative(direction)
                if (isAir(worldGenLevel, apricornPos)) {
                    apricornSpots.add(direction.opposite to apricornPos)
                }
            }

            leafPos = leafPos.above()
            setBlockIfClear(worldGenLevel, leafPos, LeavesBlock.updateDistance(leafBlock, worldGenLevel, leafPos))

            for (direction in listOf(NORTH, EAST, SOUTH, WEST)) {
                val apricornPos = leafPos.relative(direction)
                if (isAir(worldGenLevel, apricornPos)) {
                    apricornSpots.add(direction.opposite to apricornPos)
                }
            }

            allApricornSpots.add(apricornSpots)
        }

        // Topper
        val topperPos = origin.offset(0, 5, 0)
        setBlockIfClear(worldGenLevel, topperPos, LeavesBlock.updateDistance(leafBlock, worldGenLevel, topperPos))

        for (direction in Lists.newArrayList(NORTH, EAST, SOUTH, WEST)) {
            val leafPos = topperPos.relative(direction)
            setBlockIfClear(worldGenLevel, leafPos, LeavesBlock.updateDistance(leafBlock, worldGenLevel, leafPos))
        }

        for (blocks in getLayerFourVariation(origin.relative(UP, 4), random)) {
            for (block in blocks) {
                setBlockIfClear(worldGenLevel, block, LeavesBlock.updateDistance(leafBlock, worldGenLevel, block))
            }
        }

        if (allApricornSpots.isNotEmpty()) {
            allApricornSpots.filter(List<*>::isNotEmpty)
                .randomNoCopy(allApricornSpots.size.coerceAtMost(8))
                .map { it.random() }
                .forEach {
                    if(worldGenLevel.getBlockState(it.second.relative(it.first)).block.equals(leafBlock.block)) {
                        setBlockIfClear(
                            worldGenLevel,
                            it.second,
                            context.config().state
                                .setValue(HorizontalDirectionalBlock.FACING, it.first)
                                .setValue(
                                    ApricornBlock.AGE,
                                    if (isGenerating) random.nextInt(ApricornBlock.MAX_AGE + 1) else 0
                                )
                        )
                    }
                }
        }
        return true
    }

    private fun setBlockIfClear(worldGenLevel: WorldGenLevel, blockPos: BlockPos, blockState: BlockState) {
        if (!TreeFeature.isAirOrLeaves(worldGenLevel, blockPos)) {
            return
        }
        worldGenLevel.setBlock(blockPos, blockState, 3)
    }

    private fun getLayerOneVariation(origin: BlockPos, random: RandomSource): Pair<BlockPos, BlockPos> {
        var direction = NORTH
        when (random.nextInt(4)) {
            1 -> direction = EAST
            2 -> direction = SOUTH
            3 -> direction = WEST
        }
        val posOne = origin.offset(direction.stepX * 2, direction.stepY * 2, direction.stepZ * 2)
        val offset = if (random.nextBoolean()) -1 else 1
        val posTwo = if (direction.stepX == 0) posOne.offset(offset, 0, 0) else posOne.offset(0, 0, offset)
        return posOne to posTwo
    }

    private fun getLayerFourVariation(origin: BlockPos, random: RandomSource): List<List<BlockPos>> {
        val variationList = mutableListOf<List<BlockPos>>()
        val usedDirections = mutableListOf<Direction>()

        for (i in 1..nextInt(2,4)) {
            var direction: Direction? = null

            while (direction == null || usedDirections.contains(direction)) {
                when (random.nextInt(4)) {
                    0 -> direction = NORTH
                    1 -> direction = EAST
                    2 -> direction = SOUTH
                    3 -> direction = WEST
                }
            }

            val posOne = origin.offset(direction.stepX * 2, direction.stepY * 2, direction.stepZ * 2)
            val offset = if (random.nextBoolean()) -1 else 1
            val posTwo = if (direction.stepX == 0) posOne.offset(offset, 0, 0) else posOne.offset(0, 0, offset)
            if (random.nextInt(3) == 0) {
                variationList.add(listOf(posOne, posTwo))
            } else {
                variationList.add(listOf(if (random.nextBoolean()) posOne else posTwo))
            }
        }
        return variationList
    }

    private fun isAir(testableWorld: LevelSimulatedReader, blockPos: BlockPos?): Boolean {
        return testableWorld.isStateAtPosition(blockPos) { blockState: BlockState ->
            blockState.`is`(Blocks.AIR)
        }
    }

}