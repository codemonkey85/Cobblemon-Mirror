/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.api.multiblock.MultiblockBlock
import com.cobblemon.mod.common.api.multiblock.MultiblockEntity
import com.cobblemon.mod.common.block.entity.FossilMultiblockEntity
import com.cobblemon.mod.common.block.multiblock.FossilMultiblockBuilder
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.StringRepresentable
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

class MonitorBlock(settings: Properties) : MultiblockBlock(settings) {
    init {
        registerDefaultState(stateDefinition.any()
            .setValue(HORIZONTAL_FACING, Direction.NORTH))
    }

    override fun createMultiBlockEntity(
        pos: BlockPos,
        state: BlockState
    ): FossilMultiblockEntity {
        return FossilMultiblockEntity(
            pos, state, FossilMultiblockBuilder(pos)
        )
    }

    override fun codec(): MapCodec<out BaseEntityBlock> {
        return CODEC
    }

    override fun getStateForPlacement(blockPlaceContext: BlockPlaceContext): BlockState {
        return defaultBlockState().setValue(HORIZONTAL_FACING, blockPlaceContext.horizontalDirection)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(HORIZONTAL_FACING)
        builder.add(SCREEN)
    }

    override fun hasAnalogOutputSignal(state: BlockState): Boolean {
        // TODO: return false if not attached to a multiblock structure
        return true
    }

    override fun getAnalogOutputSignal(state: BlockState, world: Level, pos: BlockPos): Int {
        val monitorEntity = world.getBlockEntity(pos) as? MultiblockEntity
        val multiBlockEntity = monitorEntity?.multiblockStructure
        if(multiBlockEntity != null) {
            return multiBlockEntity.getAnalogOutputSignal(state, world, pos)
        }
        return 0
    }

    override fun getShape(
        state: BlockState,
        blockGetter: BlockGetter,
        pos: BlockPos,
        collisionContext: CollisionContext
    ): VoxelShape {
        return HITBOX
    }

    @Deprecated("Deprecated in Java")
    override fun rotate(state: BlockState, rotation: Rotation): BlockState {
        return state.setValue(HORIZONTAL_FACING, rotation.rotate(state.getValue(HORIZONTAL_FACING)))
    }

    @Deprecated("Deprecated in Java")
    override fun isPathfindable(state: BlockState, type: PathComputationType): Boolean {
        return false
    }

    companion object {
        val CODEC = simpleCodec(::MonitorBlock)

        //0 is off
        val SCREEN = EnumProperty.create("screen", MonitorScreen::class.java)
        val HITBOX = Shapes.or(
            Shapes.box(0.0625, 0.0, 0.0625, 0.9375, 0.375, 0.9375),
            Shapes.box(0.0625, 0.875, 0.0625, 0.9375, 1.0, 0.9375),
            Shapes.box(0.8125, 0.375, 0.0625, 0.9375, 0.875, 0.9375),
            Shapes.box(0.1875, 0.375, 0.125, 0.8125, 0.875, 0.9375),
            Shapes.box(0.0625, 0.375, 0.0625, 0.1875, 0.875, 0.9375)
        )
    }
    enum class MonitorScreen : StringRepresentable {
        OFF,
        BLUE_PROGRESS_1,
        BLUE_PROGRESS_2,
        BLUE_PROGRESS_3,
        BLUE_PROGRESS_4,
        BLUE_PROGRESS_5,
        BLUE_PROGRESS_6,
        BLUE_PROGRESS_7,
        BLUE_PROGRESS_8,
        BLUE_PROGRESS_9,
        GREEN_PROGRESS_9;

        override fun getSerializedName(): String = this.name.lowercase()
    }
}