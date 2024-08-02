/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.block.entity.NestBlockEntity
import com.cobblemon.mod.common.client.gui.PartyOverlay.Companion.state
import com.mojang.serialization.MapCodec
import dev.lambdaurora.lambdynlights.util.SodiumDynamicLightHandler.pos
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.entity.player.Player
import net.minecraft.world.InteractionHand
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.core.BlockPos
import net.minecraft.util.StringRepresentable
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.SimpleWaterloggedBlock
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.material.Fluids

class NestBlock(val variant: NestVariant, properties: Properties) : BaseEntityBlock(properties), SimpleWaterloggedBlock {

    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(BlockStateProperties.WATERLOGGED, false)
        )
    }

    enum class NestVariant(val id: String) : StringRepresentable {
        CAVE("cave_nest"),
        NETHER("nether_nest"),
        WATER("water_nest"),
        BASIC("basic_nest");

        override fun getSerializedName() = id
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(BlockStateProperties.WATERLOGGED)
    }

    override fun useWithoutItem(
        blockState: BlockState,
        level: Level,
        blockPos: BlockPos,
        player: Player,
        blockHitResult: BlockHitResult
    ): InteractionResult {
        super.useWithoutItem(blockState, level, blockPos, player, blockHitResult)

        val entity = level.getBlockEntity(blockPos) as? NestBlockEntity
        return entity?.onUse(blockState, level, blockPos, player, InteractionHand.MAIN_HAND, blockHitResult) ?: InteractionResult.FAIL
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState) = NestBlockEntity(pos, state)

    override fun getRenderShape(blockState: BlockState) = RenderShape.MODEL

    override fun canPlaceLiquid(
        player: Player?,
        level: BlockGetter,
        pos: BlockPos,
        state: BlockState,
        fluid: Fluid
    ): Boolean {
        return variant == NestVariant.WATER && super.canPlaceLiquid(player, level, pos, state, fluid)
    }

    override fun getFluidState(state: BlockState): FluidState {
        return if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            Fluids.WATER.getSource(false)
        } else super.getFluidState(state)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState {
        if (variant == NestVariant.WATER) {
            return defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, ctx.level.getFluidState(ctx.clickedPos).type == Fluids.WATER)
        }
        return defaultBlockState()
    }

    override fun <T : BlockEntity?> getTicker(
        level: Level,
        blockState: BlockState,
        blockEntityType: BlockEntityType<T>
    ): BlockEntityTicker<T>? = createTickerHelper(blockEntityType, CobblemonBlockEntities.NEST, NestBlockEntity.TICKER::tick)

    override fun codec(): MapCodec<out BaseEntityBlock> {
        TODO("Not yet implemented")
    }
}