/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.item.MintLeafItem
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.BlockTags
import net.minecraft.util.RandomSource
import net.minecraft.util.StringRepresentable
import net.minecraft.world.item.Item
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.BonemealableBlock
import net.minecraft.world.level.block.CropBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

@Suppress("OVERRIDE_DEPRECATION", "MemberVisibilityCanBePrivate")
class MintBlock(private val mintType: MintType, settings: Properties) : CropBlock(settings), BonemealableBlock {

    init {
        registerDefaultState(stateDefinition.any()
            .setValue(AGE, 0)
            .setValue(IS_WILD, false))
    }

    // DO NOT use withAge
    // Explanation for these 2 beautiful copy pasta are basically that we need to keep the blockstate and that's not possible with the default impl :(
    override fun randomTick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
        if (world.getRawBrightness(pos, 0) < 9 || this.isMaxAge(state) || random.nextInt(8) != 0) {
            return
        }
        this.growCrops(world, pos, state, false)
    }

    override fun growCrops(world: Level, pos: BlockPos, state: BlockState) {
        this.growCrops(world, pos, state, true)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(AGE)
        builder.add(IS_WILD)
    }

    override fun canSurvive(state: BlockState, world: LevelReader, pos: BlockPos): Boolean {
        val floor = world.getBlockState(pos.below())
        // A bit of a copy pasta but we don't have access to the BlockState being attempted to be placed above on the canPlantOnTop
        return (world.getRawBrightness(pos, 0) >= 8 || world.canSeeSky(pos)) && ((this.isWild(state) && floor.`is`(BlockTags.DIRT)) || this.mayPlaceOn(floor, world, pos))
    }

    override fun getBaseSeedId(): ItemLike = this.mintType.getSeed()

    override fun getBonemealAgeIncrease(world: Level): Int = 1

    override fun getShape(state: BlockState, world: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = AGE_TO_SHAPE[this.getAge(state)]

    fun isWild(state: BlockState): Boolean = state.getValue(IS_WILD)

    private fun growCrops(world: Level, pos: BlockPos, state: BlockState, useRandomGrowthAmount: Boolean) {
        val growthAmount = if (useRandomGrowthAmount) this.getBonemealAgeIncrease(world) else 1
        val newAge = (this.getAge(state) + growthAmount).coerceAtMost(MATURE_AGE)
        world.setBlock(pos, state.setValue(AGE, newAge), UPDATE_CLIENTS)
    }

    override fun codec(): MapCodec<out CropBlock> {
        return CODEC
    }

    @Suppress("unused")
    enum class MintType : StringRepresentable {

        RED,
        BLUE,
        CYAN,
        PINK,
        GREEN,
        WHITE;

        fun getSeed(): Item = when (this) {
            RED -> CobblemonItems.RED_MINT_SEEDS
            BLUE -> CobblemonItems.BLUE_MINT_SEEDS
            CYAN -> CobblemonItems.CYAN_MINT_SEEDS
            PINK -> CobblemonItems.PINK_MINT_SEEDS
            GREEN -> CobblemonItems.GREEN_MINT_SEEDS
            WHITE -> CobblemonItems.WHITE_MINT_SEEDS
        }

        fun getLeaf(): MintLeafItem = when (this) {
            RED -> CobblemonItems.RED_MINT_LEAF
            BLUE -> CobblemonItems.BLUE_MINT_LEAF
            CYAN -> CobblemonItems.CYAN_MINT_LEAF
            PINK -> CobblemonItems.PINK_MINT_LEAF
            GREEN -> CobblemonItems.GREEN_MINT_LEAF
            WHITE -> CobblemonItems.WHITE_MINT_LEAF
        }

        fun getCropBlock(): MintBlock = when (this) {
            RED -> CobblemonBlocks.RED_MINT
            BLUE -> CobblemonBlocks.BLUE_MINT
            CYAN -> CobblemonBlocks.CYAN_MINT
            PINK -> CobblemonBlocks.PINK_MINT
            GREEN -> CobblemonBlocks.GREEN_MINT
            WHITE -> CobblemonBlocks.WHITE_MINT
        }

        override fun getSerializedName(): String = this.name.lowercase()

        companion object {
            val CODEC = StringRepresentable.fromEnum(::values)
        }
    }

    companion object {
        val CODEC: MapCodec<MintBlock> = RecordCodecBuilder.mapCodec { it.group(
            MintType.CODEC.fieldOf("mintType").forGetter(MintBlock::mintType),
            propertiesCodec()
        ).apply(it, ::MintBlock) }

        val AGE: IntegerProperty = CropBlock.AGE
        const val MATURE_AGE = MAX_AGE
        val IS_WILD: BooleanProperty = BooleanProperty.create("is_wild")

        private val AGE_0_SHAPE = Shapes.box(0.0, -0.9, 0.0, 1.0, 0.1, 1.0)
        private val AGE_1_TO_2_SHAPE = Shapes.box(0.0, -0.9, 0.0, 1.0, 0.2, 1.0)
        private val AGE_3_SHAPE = Shapes.box(0.0, -0.9, 0.0, 1.0, 0.3, 1.0)
        private val AGE_4_SHAPE = Shapes.box(0.0, -0.9, 0.0, 1.0, 0.4, 1.0)
        private val AGE_5_SHAPE = Shapes.box(0.0, -0.9, 0.0, 1.0, 0.5, 1.0)
        private val AGE_6_SHAPE = Shapes.box(0.0, -0.9, 0.0, 1.0, 0.6, 1.0)
        private val AGE_7_SHAPE = Shapes.box(0.0, -0.9, 0.0, 1.0, 0.7, 1.0)

        val AGE_TO_SHAPE = arrayOf(
            AGE_0_SHAPE,
            AGE_1_TO_2_SHAPE,
            AGE_1_TO_2_SHAPE,
            AGE_3_SHAPE,
            AGE_4_SHAPE,
            AGE_5_SHAPE,
            AGE_6_SHAPE,
            AGE_7_SHAPE
        )

    }
}