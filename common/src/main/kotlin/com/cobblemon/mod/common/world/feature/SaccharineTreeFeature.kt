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
import com.cobblemon.mod.common.util.randomNoCopy
import com.google.common.collect.Lists
import kotlin.random.Random
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.HorizontalFacingBlock
import net.minecraft.block.LeavesBlock
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.*
import net.minecraft.world.StructureWorldAccess
import net.minecraft.world.TestableWorld
import net.minecraft.world.chunk.ChunkStatus
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.SingleStateFeatureConfig
import net.minecraft.world.gen.feature.TreeFeature
import net.minecraft.world.gen.feature.util.FeatureContext

class SaccharineTreeFeature : Feature<SingleStateFeatureConfig>(SingleStateFeatureConfig.CODEC) {

    override fun generate(context: FeatureContext<SingleStateFeatureConfig>): Boolean {
        val worldGenLevel: StructureWorldAccess = context.world
        val random = context.random
        val origin = context.origin

        val isGenerating = worldGenLevel.getChunk(origin).status != ChunkStatus.FULL

        if (isGenerating) {
            val biome = worldGenLevel.getBiome(origin)
            val multiplier = if (biome.isIn(CobblemonBiomeTags.HAS_APRICORNS_SPARSE)) {
                0.1F
            } else if (biome.isIn(CobblemonBiomeTags.HAS_APRICORNS_DENSE)) {
                10F
            } else if (biome.isIn(CobblemonBiomeTags.HAS_APRICORNS_NORMAL)) {
                1.0F
            } else {
                return false
            }

            if (random.nextFloat() > multiplier * Cobblemon.config.baseApricornTreeGenerationChance) {
                return false
            }
        }

        if (!worldGenLevel.getBlockState(origin.down()).isIn(BlockTags.DIRT)) {
            return false
        }

        val potentialBeehivePositions = mutableListOf<BlockPos>()

        // Create trunk (1 or 2 blocks tall)
        val logState = CobblemonBlocks.SACCHARINE_LOG.defaultState
        val trunkHeight = if (random.nextBoolean()) 1 else 2
        for (y in 0 until trunkHeight) {
            val logPos = origin.offset(UP, y)
            worldGenLevel.setBlockState(logPos, logState, 2)
        }

        val saccharineLeaf = CobblemonBlocks.SACCHARINE_LEAVES.defaultState // SaccharineLeafBlock
        var currentHeight = trunkHeight

        // Top Trunk Pattern
        placeTopTrunkPattern(worldGenLevel, origin.offset(UP, currentHeight), logState, potentialBeehivePositions)
        currentHeight++

        // Leaf Start Pattern
        placeLeafStartPattern(worldGenLevel, origin.offset(UP, currentHeight), logState, CobblemonBlocks.SACCHARINE_LEAVES.defaultState, potentialBeehivePositions, Random)
        currentHeight++

        // Big Leaf Pattern
        placeBigLeafPattern(worldGenLevel, origin.offset(UP, currentHeight), logState, CobblemonBlocks.SACCHARINE_LEAVES.defaultState)
        currentHeight++

        // Small Leaf Pattern
        placeSmallLeafPattern(worldGenLevel, origin.offset(UP, currentHeight), logState, CobblemonBlocks.SACCHARINE_LEAVES.defaultState, Blocks.AIR.defaultState, Random)
        currentHeight++

        // Big Leaf Pattern
        placeBigLeafPattern(worldGenLevel, origin.offset(UP, currentHeight), logState, CobblemonBlocks.SACCHARINE_LEAVES.defaultState)
        currentHeight++

        // Random extension or Leaf Topper Pattern
        if (random.nextBoolean()) {
            placeLeafTopperPattern(worldGenLevel, origin.offset(UP, currentHeight), CobblemonBlocks.SACCHARINE_LEAVES.defaultState, CobblemonBlocks.SACCHARINE_LEAVES.defaultState, Random)
        } else {
            // Small Leaf Pattern
            placeSmallLeafPattern(worldGenLevel, origin.offset(UP, currentHeight), logState, CobblemonBlocks.SACCHARINE_LEAVES.defaultState, Blocks.AIR.defaultState, Random)
            currentHeight++

            // Big Leaf Pattern
            placeBigLeafPattern(worldGenLevel, origin.offset(UP, currentHeight), logState, CobblemonBlocks.SACCHARINE_LEAVES.defaultState)
            currentHeight++

            // Leaf Topper Pattern
            placeLeafTopperPattern(worldGenLevel, origin.offset(UP, currentHeight), CobblemonBlocks.SACCHARINE_LEAVES.defaultState, CobblemonBlocks.SACCHARINE_LEAVES.defaultState, Random)
        }

        // Check for flowers within a 5-block radius to place a beehive
        if (isFlowerNearby(worldGenLevel, origin)) {
            placeBeehive(worldGenLevel, Random, potentialBeehivePositions)
        }

        return true
    }

    private fun placeBigLeafPattern(worldGenLevel: StructureWorldAccess, origin: BlockPos, logBlock: BlockState, leafBlock: BlockState) {
        val positions = listOf(
            origin.add(-2, 0, 0),
            origin.add(2, 0, 0),
            origin.add(0, 0, -2),
            origin.add(0, 0, 2),
            origin.add(-1, 0, -2),
            origin.add(1, 0, -2),
            origin.add(-1, 0, 2),
            origin.add(1, 0, 2),
            origin.add(-2, 0, -1),
            origin.add(-2, 0, 1),
            origin.add(2, 0, -1),
            origin.add(2, 0, 1),
            origin.add(-1, 0, -1),
            origin.add(1, 0, 1),
            origin.add(-1, 0, 1),
            origin.add(1, 0, -1),
            origin.add(0, 0, 0), // Add center position
            origin.add(1, 0, 0),
            origin.add(-1, 0, 0),
            origin.add(0, 0, 1),
            origin.add(0, 0, -1),
            origin.add(0, 0, 0)
        )

        for (pos in positions) {
            setBlockIfClear(worldGenLevel, pos, leafBlock.with(LeavesBlock.DISTANCE, 2))
        }

        // Center trunk
        worldGenLevel.setBlockState(origin, logBlock, 2)
    }

    private fun placeSmallLeafPattern(worldGenLevel: StructureWorldAccess, origin: BlockPos, logBlock: BlockState, leafBlock: BlockState, specialBlock: BlockState, random: Random) {
        val positions = listOf(
            origin.add(-1, 0, 0),
            origin.add(1, 0, 0),
            origin.add(0, 0, -1),
            origin.add(0, 0, 1)
        )

        for (pos in positions) {
            setBlockIfClear(worldGenLevel, pos, leafBlock.with(LeavesBlock.DISTANCE, 2))
        }

        val specialPositions = listOf(
            origin.add(-1, 0, -1),
            origin.add(1, 0, 1),
            origin.add(-1, 0, 1),
            origin.add(1, 0, -1)
        )

        for (pos in specialPositions) {
            if (random.nextFloat() < 0.25f) {
                setBlockIfClear(worldGenLevel, pos, specialBlock)
            } else {
                setBlockIfClear(worldGenLevel, pos, leafBlock.with(LeavesBlock.DISTANCE, 2))
            }
        }

        // Center trunk
        worldGenLevel.setBlockState(origin, logBlock, 2)
    }

    private fun placeLeafTopperPattern(worldGenLevel: StructureWorldAccess, origin: BlockPos, leafBlock: BlockState, specialBlock: BlockState, random: Random) {
        val positions = listOf(
            origin.add(-1, 0, 0),
            origin.add(1, 0, 0),
            origin.add(0, 0, -1),
            origin.add(0, 0, 1),
            origin.add(-1, 0, -1),
            origin.add(1, 0, 1),
            origin.add(-1, 0, 1),
            origin.add(1, 0, -1)
        )

        for (pos in positions) {
            if (random.nextFloat() < 0.25f) {
                setBlockIfClear(worldGenLevel, pos, specialBlock)
            } else {
                setBlockIfClear(worldGenLevel, pos, leafBlock.with(LeavesBlock.DISTANCE, 2))
            }
        }

        // Center leaf
        setBlockIfClear(worldGenLevel, origin, leafBlock.with(LeavesBlock.DISTANCE, 2))
    }

    private fun placeLeafStartPattern(worldGenLevel: StructureWorldAccess, origin: BlockPos, logBlock: BlockState, leafBlock: BlockState, potentialBeehivePositions: MutableList<BlockPos>, random: Random) {
        val positions = listOf(
            origin.add(-1, 0, 0),
            origin.add(1, 0, 0),
            origin.add(0, 0, -1),
            origin.add(0, 0, 1),
            origin.add(-1, 0, -1),
            origin.add(1, 0, 1),
            origin.add(-1, 0, 1),
            origin.add(1, 0, -1)
        )

        for (pos in positions) {
            if (random.nextFloat() < 0.25f) {
                potentialBeehivePositions.add(pos)
            } else {
                setBlockIfClear(worldGenLevel, pos, leafBlock.with(LeavesBlock.DISTANCE, 2))
            }
        }

        // Center trunk
        worldGenLevel.setBlockState(origin, logBlock, 2)
    }

    private fun placeTopTrunkPattern(worldGenLevel: StructureWorldAccess, origin: BlockPos, logBlock: BlockState, potentialBeehivePositions: MutableList<BlockPos>) {
        val positions = listOf(
            origin.add(-1, 0, 0),
            origin.add(1, 0, 0),
            origin.add(0, 0, -1),
            origin.add(0, 0, 1)
        )

        for (pos in positions) {
            potentialBeehivePositions.add(pos)
        }

        // Center trunk
        worldGenLevel.setBlockState(origin, logBlock, 2)
    }

    private fun isFlowerNearby(worldGenLevel: StructureWorldAccess, origin: BlockPos): Boolean {
        for (dx in -5..5) {
            for (dz in -5..5) {
                for (dy in -5..5) {
                    val pos = origin.add(dx, dy, dz)
                    if (worldGenLevel.getBlockState(pos).isIn(BlockTags.FLOWERS)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun placeBeehive(worldGenLevel: StructureWorldAccess, random: Random, potentialBeehivePositions: MutableList<BlockPos>) {
        if (potentialBeehivePositions.isNotEmpty()) {
            val hivePos = potentialBeehivePositions[random.nextInt(potentialBeehivePositions.size)]
            setBlockIfClear(worldGenLevel, hivePos, Blocks.BEE_NEST.defaultState)
        }
    }

    private fun setBlockIfClear(worldGenLevel: StructureWorldAccess, blockPos: BlockPos, blockState: BlockState) {
        if (!TreeFeature.isAirOrLeaves(worldGenLevel, blockPos)) {
            return
        }
        worldGenLevel.setBlockState(blockPos, blockState, 3)
    }

    private fun isAir(testableWorld: TestableWorld, blockPos: BlockPos?): Boolean {
        return testableWorld.testBlockState(
            blockPos
        ) { blockState: BlockState ->
            blockState.isOf(
                Blocks.AIR
            )
        }
    }
}
