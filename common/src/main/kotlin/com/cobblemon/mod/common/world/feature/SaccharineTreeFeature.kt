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
import com.cobblemon.mod.common.block.SaccharineLeafBlock
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
import kotlin.random.Random
import kotlin.random.Random.Default.nextInt

class SaccharineTreeFeature : Feature<BlockStateConfiguration>(BlockStateConfiguration.CODEC) {

    override fun place(context: FeaturePlaceContext<BlockStateConfiguration>): Boolean {
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

        val potentialBeehivePositions = mutableListOf<BlockPos>()

        // Create trunk (1 or 2 blocks tall)
        val logState = CobblemonBlocks.SACCHARINE_LOG.defaultBlockState()
        val trunkHeight = if (random.nextBoolean()) 1 else 2
        for (y in 0 until trunkHeight) {
            val logPos = origin.relative(UP, y)
            worldGenLevel.setBlock(logPos, logState, 2)
        }

        val saccharineLeaf = CobblemonBlocks.SACCHARINE_LEAVES.defaultBlockState() // SaccharineLeafBlock
        var currentHeight = trunkHeight

        // Top Trunk Pattern
        placeTopTrunkPattern(worldGenLevel, origin.relative(UP, currentHeight), logState, potentialBeehivePositions)
        currentHeight++

        // Leaf Start Pattern
        placeLeafStartPattern(worldGenLevel, origin.relative(UP, currentHeight), logState, CobblemonBlocks.SACCHARINE_LEAVES.defaultBlockState(), potentialBeehivePositions, Random)
        currentHeight++

        // Big Leaf Pattern
        placeBigLeafPattern(worldGenLevel, origin.relative(UP, currentHeight), logState, CobblemonBlocks.SACCHARINE_LEAVES.defaultBlockState())
        currentHeight++

        // Small Leaf Pattern
        placeSmallLeafPattern(worldGenLevel, origin.relative(UP, currentHeight), logState, CobblemonBlocks.SACCHARINE_LEAVES.defaultBlockState(), Blocks.AIR.defaultBlockState(), Random)
        currentHeight++

        // Big Leaf Pattern
        placeBigLeafPattern(worldGenLevel, origin.relative(UP, currentHeight), logState, CobblemonBlocks.SACCHARINE_LEAVES.defaultBlockState())
        currentHeight++

        // Random extension or Leaf Topper Pattern
        if (random.nextBoolean()) {
            placeLeafTopperPattern(worldGenLevel, origin.relative(UP, currentHeight), CobblemonBlocks.SACCHARINE_LEAVES.defaultBlockState(), CobblemonBlocks.SACCHARINE_LEAVES.defaultBlockState(), Random)
        } else {
            // Small Leaf Pattern
            placeSmallLeafPattern(worldGenLevel, origin.relative(UP, currentHeight), logState, CobblemonBlocks.SACCHARINE_LEAVES.defaultBlockState(), Blocks.AIR.defaultBlockState(), Random)
            currentHeight++

            // Big Leaf Pattern
            placeBigLeafPattern(worldGenLevel, origin.relative(UP, currentHeight), logState, CobblemonBlocks.SACCHARINE_LEAVES.defaultBlockState())
            currentHeight++

            // Leaf Topper Pattern
            placeLeafTopperPattern(worldGenLevel, origin.relative(UP, currentHeight), CobblemonBlocks.SACCHARINE_LEAVES.defaultBlockState(), CobblemonBlocks.SACCHARINE_LEAVES.defaultBlockState(), Random)
        }

        // Check for flowers within a 5-block radius to place a beehive
        if (isFlowerNearby(worldGenLevel, origin)) {
            placeBeehive(worldGenLevel, Random, potentialBeehivePositions)
        }

        return true
    }

    private fun placeBigLeafPattern(worldGenLevel: WorldGenLevel, origin: BlockPos, logBlock: BlockState, leafBlock: BlockState) {
        val positions = listOf(
            origin.offset(-2, 0, 0),
            origin.offset(2, 0, 0),
            origin.offset(0, 0, -2),
            origin.offset(0, 0, 2),
            origin.offset(-1, 0, -2),
            origin.offset(1, 0, -2),
            origin.offset(-1, 0, 2),
            origin.offset(1, 0, 2),
            origin.offset(-2, 0, -1),
            origin.offset(-2, 0, 1),
            origin.offset(2, 0, -1),
            origin.offset(2, 0, 1),
            origin.offset(-1, 0, -1),
            origin.offset(1, 0, 1),
            origin.offset(-1, 0, 1),
            origin.offset(1, 0, -1),
            origin.offset(0, 0, 0), // Add center position
            origin.offset(1, 0, 0),
            origin.offset(-1, 0, 0),
            origin.offset(0, 0, 1),
            origin.offset(0, 0, -1),
            origin.offset(0, 0, 0)
        )

        for (pos in positions) {
            setBlockIfClear(worldGenLevel, pos, leafBlock.setValue(LeavesBlock.DISTANCE, 2))
        }

        // Center trunk
        worldGenLevel.setBlock(origin, logBlock, 2)
    }

    private fun placeSmallLeafPattern(worldGenLevel: WorldGenLevel, origin: BlockPos, logBlock: BlockState, leafBlock: BlockState, specialBlock: BlockState, random: Random) {
        val positions = listOf(
            origin.offset(-1, 0, 0),
            origin.offset(1, 0, 0),
            origin.offset(0, 0, -1),
            origin.offset(0, 0, 1)
        )

        for (pos in positions) {
            setBlockIfClear(worldGenLevel, pos, leafBlock.setValue(LeavesBlock.DISTANCE, 2))
        }

        val specialPositions = listOf(
            origin.offset(-1, 0, -1),
            origin.offset(1, 0, 1),
            origin.offset(-1, 0, 1),
            origin.offset(1, 0, -1)
        )

        for (pos in specialPositions) {
            if (random.nextFloat() < 0.25f) {
                setBlockIfClear(worldGenLevel, pos, specialBlock)
            } else {
                setBlockIfClear(worldGenLevel, pos, leafBlock.setValue(LeavesBlock.DISTANCE, 2))
            }
        }

        // Center trunk
        worldGenLevel.setBlock(origin, logBlock, 2)
    }

    private fun placeLeafTopperPattern(worldGenLevel: WorldGenLevel, origin: BlockPos, leafBlock: BlockState, specialBlock: BlockState, random: Random) {
        val positions = listOf(
            origin.offset(-1, 0, 0),
            origin.offset(1, 0, 0),
            origin.offset(0, 0, -1),
            origin.offset(0, 0, 1),
            origin.offset(-1, 0, -1),
            origin.offset(1, 0, 1),
            origin.offset(-1, 0, 1),
            origin.offset(1, 0, -1)
        )

        for (pos in positions) {
            if (random.nextFloat() < 0.25f) {
                setBlockIfClear(worldGenLevel, pos, specialBlock)
            } else {
                setBlockIfClear(worldGenLevel, pos, leafBlock.setValue(LeavesBlock.DISTANCE, 2))
            }
        }

        // Center leaf
        setBlockIfClear(worldGenLevel, origin, leafBlock.setValue(LeavesBlock.DISTANCE, 2))
    }

    private fun placeLeafStartPattern(worldGenLevel: WorldGenLevel, origin: BlockPos, logBlock: BlockState, leafBlock: BlockState, potentialBeehivePositions: MutableList<BlockPos>, random: Random) {
        val positions = listOf(
            origin.offset(-1, 0, 0),
            origin.offset(1, 0, 0),
            origin.offset(0, 0, -1),
            origin.offset(0, 0, 1),
            origin.offset(-1, 0, -1),
            origin.offset(1, 0, 1),
            origin.offset(-1, 0, 1),
            origin.offset(1, 0, -1)
        )

        for (pos in positions) {
            if (random.nextFloat() < 0.25f) {
                potentialBeehivePositions.add(pos)
            } else {
                setBlockIfClear(worldGenLevel, pos, leafBlock.setValue(LeavesBlock.DISTANCE, 2))
            }
        }

        // Center trunk
        worldGenLevel.setBlock(origin, logBlock, 2)
    }

    private fun placeTopTrunkPattern(worldGenLevel: WorldGenLevel, origin: BlockPos, logBlock: BlockState, potentialBeehivePositions: MutableList<BlockPos>) {
        val positions = listOf(
            origin.offset(-1, 0, 0),
            origin.offset(1, 0, 0),
            origin.offset(0, 0, -1),
            origin.offset(0, 0, 1)
        )

        for (pos in positions) {
            potentialBeehivePositions.add(pos)
        }

        // Center trunk
        worldGenLevel.setBlock(origin, logBlock, 2)
    }

    private fun isFlowerNearby(worldGenLevel: WorldGenLevel, origin: BlockPos): Boolean {
        for (dx in -5..5) {
            for (dz in -5..5) {
                for (dy in -5..5) {
                    val pos = origin.offset(dx, dy, dz)
                    if (worldGenLevel.getBlockState(pos).`is`(BlockTags.FLOWERS)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun placeBeehive(worldGenLevel: WorldGenLevel, random: Random, potentialBeehivePositions: MutableList<BlockPos>) {
        if (potentialBeehivePositions.isNotEmpty()) {
            val hivePos = potentialBeehivePositions[random.nextInt(potentialBeehivePositions.size)]
            setBlockIfClear(worldGenLevel, hivePos, Blocks.BEE_NEST.defaultBlockState())
        }
    }

    private fun setBlockIfClear(worldGenLevel: WorldGenLevel, blockPos: BlockPos, blockState: BlockState) {
        if (!TreeFeature.isAirOrLeaves(worldGenLevel, blockPos)) {
            return
        }
        worldGenLevel.setBlock(blockPos, blockState, 3)
    }

    /*private fun isAir(testableWorld: TestableWorld, blockPos: BlockPos?): Boolean {
        return testableWorld.testBlockState(
            blockPos
        ) { blockState: BlockState ->
            blockState.`is`(
                Blocks.AIR
            )
        }
    }*/
}
