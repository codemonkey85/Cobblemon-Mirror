/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.mulch.MulchVariant
import com.cobblemon.mod.common.api.mulch.Mulchable
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.BlockTags
import net.minecraft.util.RandomSource
import net.minecraft.util.StringRepresentable
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.CropBlock
import net.minecraft.world.level.block.FarmBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

@Suppress("OVERRIDE_DEPRECATION", "MemberVisibilityCanBePrivate", "unused")
class RevivalHerbBlock(settings: Properties) : CropBlock(settings), Mulchable {

    init {
        registerDefaultState(stateDefinition.any()
            .setValue(AGE, MIN_AGE)
            .setValue(IS_WILD, false)
            .setValue(MUTATION, Mutation.NONE))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(AGE)
        builder.add(IS_WILD)
        builder.add(MUTATION)
    }

    override fun getAgeProperty(): IntegerProperty = AGE

    override fun canSurvive(state: BlockState, world: LevelReader, pos: BlockPos): Boolean {
        val floor = world.getBlockState(pos.below())
        val block = world.getBlockState(pos)
        // A bit of a copy pasta but we don't have access to the BlockState being attempted to be placed above on the canPlantOnTop
        return (block.`is`(BlockTags.REPLACEABLE_BY_TREES) || block.`is`(Blocks.AIR) || block.`is`(this)) && ((state.getValue(IS_WILD) && floor.`is`(BlockTags.DIRT)) || this.mayPlaceOn(floor, world, pos))
    }

    override fun getShape(
        state: BlockState,
        blockGetter: BlockGetter,
        pos: BlockPos,
        collisionContext: CollisionContext
    ): VoxelShape = AGE_SHAPES.getOrNull(this.getAge(state)) ?: Shapes.block()

    override fun getBaseSeedId(): ItemLike = CobblemonItems.REVIVAL_HERB

    override fun canHaveMulchApplied(world: ServerLevel, pos: BlockPos, state: BlockState, variant: MulchVariant): Boolean =
        variant == MulchVariant.SURPRISE && this.getAge(state) <= MUTABLE_MAX_AGE && !this.isMutated(state)

    override fun applyMulch(world: ServerLevel, random: RandomSource, pos: BlockPos, state: BlockState, variant: MulchVariant) {
        val picked = Mutation.values().filterNot { it == Mutation.NONE }.random()
        world.setBlockAndUpdate(pos, state.setValue(MUTATION, picked))//.with(AGE, MUTABLE_MAX_AGE + 1))
    }

    /**
     * Resolves the current [Mutation] of the given [state].
     *
     * @param state The [BlockState] being queried.
     * @return The current [Mutation].
     */
    fun mutationOf(state: BlockState): Mutation = state.getValue(MUTATION)

    /**
     * Checks if the given [state] has a [Mutation] different from [Mutation.NONE].
     *
     * @param state The [BlockState] being queried.
     * @return If a mutation has occurred.
     */
    fun isMutated(state: BlockState): Boolean = this.mutationOf(state) != Mutation.NONE

    override fun getMaxAge(): Int = MAX_AGE

    // DO NOT use withAge
    // Explanation for these 2 beautiful copy pasta are basically that we need to keep the blockstate and that's not possible with the default impl :(
    override fun randomTick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
        if (world.getRawBrightness(pos, 0) < 9 || this.isMaxAge(state)) {
            return
        }
        val currentMoisture = getMoistureAmount(this, world, pos)
        if (random.nextInt((25F / currentMoisture).toInt() + 1) == 0) {
            this.growCrops(world, pos, state, false)
        }
    }

    /**
     * This method is a copy pasta of [CropBlock.getGrowthSpeed]. Why? Because neoforge changes the signature of this
     * vanilla method, breaking us at runtime. To prevent this, we simply avoid using the vanilla method and create our own,
     * while not ideal, it works....
     */
    fun getMoistureAmount(block: Block, level: BlockGetter, pos: BlockPos): Float {
        var growthSpeed = 1.0F
        var ground = pos.below()

        for(x in -1..1) {
            for(z in -1..1) {
                var nearbyFarmLandBoost = 0.0F
                var blockState = level.getBlockState(ground.offset(x, 0, z))
                if (blockState.`is`(Blocks.FARMLAND)) {
                    nearbyFarmLandBoost = 1.0F
                    if (blockState.getValue(FarmBlock.MOISTURE) > 0) {
                        nearbyFarmLandBoost = 3.0F
                    }
                }

                if (x != 0 || z != 0) {
                    nearbyFarmLandBoost /= 4.0F
                }

                growthSpeed += nearbyFarmLandBoost
            }
        }

        var north = pos.north()
        var south = pos.south()
        var west = pos.west()
        var east = pos.east()
        var sameBlockDirectNeighborXAxis = level.getBlockState(west).`is`(block) || level.getBlockState(east).`is`(block)
        var sameBlockDirectNeighborZAxis = level.getBlockState(north).`is`(block) || level.getBlockState(south).`is`(block)
        if (sameBlockDirectNeighborZAxis && sameBlockDirectNeighborXAxis) {
            growthSpeed /= 2.0F
        } else {
            var sameBlockDiagonallyPresent = level.getBlockState(west.north()).`is`(block) ||
                    level.getBlockState(east.north()).`is`(block) ||
                    level.getBlockState(east.south()).`is`(block) ||
                    level.getBlockState(west.south()).`is`(block)
            if (sameBlockDiagonallyPresent) {
                growthSpeed /= 2.0F
            }
        }

        return growthSpeed
    }

    override fun growCrops(world: Level, pos: BlockPos, state: BlockState) {
        this.growCrops(world, pos, state, true)
    }

    private fun growCrops(world: Level, pos: BlockPos, state: BlockState, useRandomGrowthAmount: Boolean) {
        val growthAmount = if (useRandomGrowthAmount) this.getBonemealAgeIncrease(world) else 1
        val newAge = (this.getAge(state) + growthAmount).coerceAtMost(this.maxAge)
        world.setBlock(pos, state.setValue(AGE, newAge), UPDATE_CLIENTS)
    }

    /**
     * Represents the possible mutation states of this plant.
     */
    enum class Mutation : StringRepresentable {

        NONE,
        MENTAL,
        POWER,
        WHITE,
        MIRROR;

        override fun getSerializedName(): String = name.lowercase()
    }

    companion object {
        val CODEC = simpleCodec(::RevivalHerbBlock)

        const val MIN_AGE = 0
        const val MAX_AGE = 8

        /**
         * This represents the max age the plant can be at before no longer being able to mutate.
         */
        const val MUTABLE_MAX_AGE = 6
        val AGE = IntegerProperty.create("age", MIN_AGE, MAX_AGE)
        val IS_WILD = BooleanProperty.create("is_wild")
        val MUTATION = EnumProperty.create("mutation", Mutation::class.java)
//        val MULCH = EnumProperty.of("mulch", MulchVariant::class.java)

        /**
         * The [VoxelShape] equivalent to a certain age.
         * Highest index is [MAX_AGE].
         */
        val AGE_SHAPES = arrayOf(
            Shapes.box(0.0, -0.9, 0.0, 1.0, 0.1, 1.0),
            Shapes.box(0.0, -0.9, 0.0, 1.0, 0.2, 1.0),
            Shapes.box(0.0, -0.9, 0.0, 1.0, 0.3, 1.0),
            Shapes.box(0.0, -0.9, 0.0, 1.0, 0.4, 1.0),
            Shapes.box(0.0, -0.9, 0.0, 1.0, 0.5, 1.0),
            Shapes.box(0.0, -0.9, 0.0, 1.0, 0.7, 1.0),
            Shapes.box(0.0, -0.9, 0.0, 1.0, 0.7, 1.0),
            Shapes.box(0.0, -0.9, 0.0, 1.0, 0.9, 1.0),
            Shapes.block()
        )

    }

}