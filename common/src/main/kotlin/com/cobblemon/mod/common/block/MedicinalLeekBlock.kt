/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.tags.CobblemonBlockTags
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.FluidTags
import net.minecraft.util.RandomSource
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.CropBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

@Suppress("OVERRIDE_DEPRECATION")
class MedicinalLeekBlock(settings: Properties) : CropBlock(settings) {

    override fun getAgeProperty(): IntegerProperty = AGE

    override fun getMaxAge(): Int = MATURE_AGE

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(this.ageProperty)
    }

    override fun getBaseSeedId(): ItemLike = CobblemonItems.MEDICINAL_LEEK

    override fun getShape(state: BlockState, world: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = AGE_TO_SHAPE[this.getAge(state)]

    override fun randomTick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
        // This is specified as growing fast like sugar cane
        // They have 15 age stages until they grow upwards, this is an attempt at a chance based but likely event
        if (this.isMaxAge(state) || random.nextInt(4) != 0) {
            return
        }
        this.growCrops(world, pos, state)
    }

    // These 3 are still around for the sake of compatibility, vanilla won't trigger it but some mods might
    // We implement applyGrowth & getGrowthAmount for them
    override fun isValidBonemealTarget(world: LevelReader, pos: BlockPos, state: BlockState): Boolean = !this.isMaxAge(state)

    override fun growCrops(world: Level, pos: BlockPos, state: BlockState) {
        world.setBlock(pos, state.setValue(this.ageProperty, (this.getAge(state) + 1).coerceAtMost(this.maxAge)), UPDATE_CLIENTS)
    }

    override fun getBonemealAgeIncrease(world: Level): Int = 1

    override fun canSurvive(state: BlockState, world: LevelReader, pos: BlockPos): Boolean {
        // We don't care about the sky & light level, sugar cane doesn't either
        return this.mayPlaceOn(state, world, pos)
    }

    override fun mayPlaceOn(state: BlockState, world: BlockGetter, pos: BlockPos): Boolean {
        val down = pos.below()
        val floor = world.getBlockState(down)
        val fluidState = world.getFluidState(down)
        return floor.`is`(CobblemonBlockTags.MEDICINAL_LEEK_PLANTABLE) && fluidState.amount == 8 && fluidState.`is`(FluidTags.WATER)
    }

    override fun codec(): MapCodec<out CropBlock> {
        return CODEC
    }

    companion object {
        val CODEC = simpleCodec(::MedicinalLeekBlock)

        const val MATURE_AGE = 3
        val AGE: IntegerProperty= BlockStateProperties.AGE_3
        val AGE_TO_SHAPE = arrayOf(
                box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
                box(0.0, 0.0, 0.0, 16.0, 5.0, 16.0),
                box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
                box(0.0, 0.0, 0.0, 16.0, 11.0, 16.0)
        )

    }
}