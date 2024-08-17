/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.DirectionalBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

/**
 * Used for blocks that grow similarly to Amethyst.
 *
 * @param settings Block settings
 * @param stage The stage of growth of the block
 * @param height The height of this block's hitbox
 * @param nextStage The next stage of this block's growth. Null if final stage
 *
 * @author whatsy
 */
abstract class GrowableStoneBlock(
    settings: Properties,
    val stage: Int,
    val height: Int,
    val xzOffset: Int,
    val nextStage: Block?
) : DirectionalBlock(settings) {

    private val upShape: VoxelShape = Block.box(
        xzOffset.toDouble(),
        0.0,
        xzOffset.toDouble(),
        (16 - xzOffset).toDouble(),
        height.toDouble(),
        (16 - xzOffset).toDouble())

    private val downShape: VoxelShape = box(
        xzOffset.toDouble(),
        (16 - height).toDouble(),
        xzOffset.toDouble(),
        (16 - xzOffset).toDouble(),
        16.0,
        (16 - xzOffset).toDouble())

    private val northShape: VoxelShape = box(
        xzOffset.toDouble(),
        xzOffset.toDouble(),
        (16 - height).toDouble(),
        (16 - xzOffset).toDouble(),
        (16 - xzOffset).toDouble(),
        16.0)

    private val southShape: VoxelShape = box(
        xzOffset.toDouble(),
        xzOffset.toDouble(),
        0.0,
        (16 - xzOffset).toDouble(),
        (16 - xzOffset).toDouble(),
        height.toDouble())

    private val eastShape: VoxelShape = box(
        0.0,
        xzOffset.toDouble(),
        xzOffset.toDouble(),
        height.toDouble(),
        (16 - xzOffset).toDouble(),
        (16 - xzOffset).toDouble())

    private val westShape: VoxelShape = box(
        (16 - height).toDouble(),
        xzOffset.toDouble(),
        xzOffset.toDouble(),
        16.0,
        (16 - xzOffset).toDouble(),
        (16 - xzOffset).toDouble())

    init {
        registerDefaultState(stateDefinition.any()
            .setValue(FACING, Direction.DOWN))
    }

    abstract fun canGrow(pos: BlockPos, world: BlockGetter): Boolean

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun isRandomlyTicking(state: BlockState): Boolean = stage < MAX_STAGE

    override fun randomTick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
        if (world.random.nextInt(5) == 0 && canGrow(pos, world)) {
            val block = nextStage

            if (block != null) {
                val newState = block.defaultBlockState().setValue(FACING, state.getValue(FACING)) as BlockState
                world.setBlockAndUpdate(pos, newState)
            }
        }
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? {
        var blockState = defaultBlockState()
        val worldView = ctx.level
        val blockPos = ctx.clickedPos
        blockState = blockState.setValue(FACING, ctx.clickedFace) as BlockState
        if (blockState.canSurvive(worldView, blockPos)) {
            return blockState
        }
        return null
    }

    override fun canSurvive(state: BlockState, world: LevelReader, pos: BlockPos): Boolean {
        val direction = state.getValue(FACING) as Direction
        val blockState = world.getBlockState(pos.relative(direction.opposite))
        return blockState.isFaceSturdy(world, pos, direction) // todo (techdaan): ensure this is the right mapping
    }

    override fun updateShape(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        world: LevelAccessor,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState {
        return if (direction == state.getValue(FACING).opposite && !state.canSurvive(world, pos)) Blocks.AIR.defaultBlockState()
        else super.updateShape(state, direction, neighborState, world, pos, neighborPos)
    }

    override fun getShape(state: BlockState, world: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        val direction = state.getValue(FACING) as Direction
        return when (direction) {
            Direction.NORTH -> northShape
            Direction.SOUTH -> southShape
            Direction.EAST -> eastShape
            Direction.WEST -> westShape
            Direction.DOWN -> downShape
            Direction.UP -> upShape
            else -> upShape
        }
    }

    companion object {
        const val STAGE_0 = 0
        const val STAGE_1 = 1
        const val STAGE_2 = 2
        const val STAGE_3 = 3

        const val MAX_STAGE = STAGE_3
        const val MIN_STAGE = STAGE_0
    }
}