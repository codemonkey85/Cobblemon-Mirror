/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.multiblock

import com.cobblemon.mod.common.block.entity.FossilMultiblockEntity
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionResult
import net.minecraft.world.level.LevelReader
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

/**
 * A block that can be part of a [MultiblockStructure]
 */
abstract class MultiblockBlock(properties: Properties) : BaseEntityBlock(properties) {

    override fun setPlacedBy(
        world: Level,
        pos: BlockPos,
        state: BlockState,
        placer: LivingEntity?,
        itemStack: ItemStack
    ) {
        if (world is ServerLevel) {
            val multiblockEntity = world.getBlockEntity(pos) as? MultiblockEntity
            multiblockEntity?.multiblockBuilder?.validate(world)
        }
        super.setPlacedBy(world, pos, state, placer, itemStack)
    }

    override fun useWithoutItem(
        state: BlockState,
        world: Level,
        pos: BlockPos,
        player: Player,
        hit: BlockHitResult
    ): InteractionResult {
        val entity = world.getBlockEntity(pos) as MultiblockEntity?
        if (entity?.multiblockStructure != null) {
            return entity.multiblockStructure!!.useWithoutItem(state, world, pos, player, hit)
        }
        return super.useWithoutItem(state, world, pos, player, hit)
    }

    override fun onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, movedByPiston: Boolean) {
        val entity = level.getBlockEntity(pos)
        if (entity is MultiblockEntity && entity.multiblockStructure != null && state.block != newState.block) {
            entity.multiblockStructure!!.playerWillDestroy(level, pos, state, null)
        }
        super.onRemove(state, level, pos, newState, movedByPiston)
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? {
        return createMultiBlockEntity(pos, state)
    }

    @Deprecated("Deprecated in Java")
    override fun getRenderShape(state: BlockState): RenderShape {
        return RenderShape.MODEL
    }

    //This is done so a block picked with NBT doesnt absolutely DESTROY multiblocks
    override fun getCloneItemStack(world: LevelReader, pos: BlockPos, state: BlockState): ItemStack {
        val blockEntity = world.getBlockEntity(pos) as? MultiblockEntity ?: return ItemStack.EMPTY
        return if (blockEntity.multiblockStructure == null) super.getCloneItemStack(world, pos, state) else ItemStack.EMPTY
    }

    abstract fun createMultiBlockEntity(pos: BlockPos, state: BlockState): FossilMultiblockEntity

}