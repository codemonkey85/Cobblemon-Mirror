/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.api.multiblock.MultiblockBlock
import com.cobblemon.mod.common.block.entity.FossilAnalyzerBlockEntity
import com.cobblemon.mod.common.block.entity.FossilMultiblockEntity
import com.cobblemon.mod.common.block.multiblock.FossilMultiblockBuilder
import com.cobblemon.mod.common.block.multiblock.FossilMultiblockStructure
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.WorldlyContainerHolder
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty

class FossilAnalyzerBlock(settings: Properties) : MultiblockBlock(settings), WorldlyContainerHolder {
    init {
        registerDefaultState(stateDefinition.any()
            .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
            .setValue(ON, false))
    }

    override fun createMultiBlockEntity(pos: BlockPos, state: BlockState): FossilMultiblockEntity {
        return FossilAnalyzerBlockEntity(pos, state, FossilMultiblockBuilder(pos))
    }

    override fun codec(): MapCodec<out BaseEntityBlock> {
        return CODEC
    }

    override fun <T : BlockEntity?> getTicker(
        world: Level,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T>? = createTickerHelper(type, CobblemonBlockEntities.FOSSIL_ANALYZER, FossilMultiblockStructure.TICKER::tick)

    override fun getStateForPlacement(blockPlaceContext: BlockPlaceContext): BlockState {
        return defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, blockPlaceContext.horizontalDirection)
    }

    override fun getContainer(
        state: BlockState,
        world: LevelAccessor,
        pos: BlockPos
    ): WorldlyContainer {
        val analyzerEntity = world.getBlockEntity(pos) as FossilAnalyzerBlockEntity

        return analyzerEntity.inv
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(HorizontalDirectionalBlock.FACING)
        builder.add(ON)
    }

    companion object {
        val ON = BooleanProperty.create("on")

        val CODEC = simpleCodec(::FossilAnalyzerBlock)
    }
}