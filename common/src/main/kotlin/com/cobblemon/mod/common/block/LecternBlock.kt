/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.block.entity.LecternBlockEntity
import com.cobblemon.mod.common.item.PokedexItem
import com.cobblemon.mod.common.net.messages.client.ui.PokedexUIPacket
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.DirectionProperty
import net.minecraft.world.level.block.state.properties.Property
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.level.block.LecternBlock as MinecraftLecternBlock

class LecternBlock(properties: Properties): BaseEntityBlock(properties) {
    companion object {
        val CODEC: MapCodec<LecternBlock> = simpleCodec(::LecternBlock)
        val FACING: DirectionProperty = HorizontalDirectionalBlock.FACING
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH))
    }

    override fun codec() = CODEC

    override fun getRenderShape(blockState: BlockState?) = RenderShape.MODEL

    override fun getOcclusionShape(blockState: BlockState?, blockGetter: BlockGetter?, blockPos: BlockPos?): VoxelShape = MinecraftLecternBlock.SHAPE_COMMON

    override fun useShapeForLightOcclusion(blockState: BlockState?) = true

    override fun getCollisionShape(blockState: BlockState?, blockGetter: BlockGetter?, blockPos: BlockPos?, collisionContext: CollisionContext?): VoxelShape = MinecraftLecternBlock.SHAPE_COLLISION

    override fun getShape(blockState: BlockState, blockGetter: BlockGetter?, blockPos: BlockPos?, collisionContext: CollisionContext?): VoxelShape {
        return when (blockState.getValue(FACING) as Direction) {
            Direction.NORTH -> MinecraftLecternBlock.SHAPE_NORTH
            Direction.SOUTH -> MinecraftLecternBlock.SHAPE_SOUTH
            Direction.EAST -> MinecraftLecternBlock.SHAPE_EAST
            Direction.WEST -> MinecraftLecternBlock.SHAPE_WEST
            else -> MinecraftLecternBlock.SHAPE_COMMON
        }
    }

    override fun getStateForPlacement(blockPlaceContext: BlockPlaceContext): BlockState = this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, blockPlaceContext.horizontalDirection.opposite)

    override fun getCloneItemStack(levelReader: LevelReader, blockPos: BlockPos, blockState: BlockState): ItemStack = ItemStack(Blocks.LECTERN)

    override fun rotate(blockState: BlockState, rotation: Rotation) = blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING) as Direction)) as BlockState

    override fun mirror(blockState: BlockState, mirror: Mirror): BlockState = blockState.rotate(mirror.getRotation(blockState.getValue(FACING) as Direction))

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block?, BlockState?>) {
        builder.add(*arrayOf<Property<*>>(FACING))
    }

    override fun newBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity = LecternBlockEntity(blockPos, blockState)

    override fun isSignalSource(blockState: BlockState?): Boolean = true

    override fun getSignal(blockState: BlockState?, blockGetter: BlockGetter?, blockPos: BlockPos?, direction: Direction?): Int = 15

    override fun playerWillDestroy(level: Level, blockPos: BlockPos, blockState: BlockState, player: Player): BlockState {
        if (!level.isClientSide) {
            val blockEntity = level.getBlockEntity(blockPos)
            if (blockEntity is LecternBlockEntity && !player.isCreative) {
                val direction = blockState.getValue(FACING) as Direction
                val f = 0.25F * direction.stepX.toFloat()
                val g = 0.25F * direction.stepZ.toFloat()

                val itemEntity = ItemEntity(level, blockPos.x.toDouble() + 0.5 + f.toDouble(), (blockPos.y + 1).toDouble(), blockPos.z.toDouble() + 0.5 + g.toDouble(), blockEntity.removeItemStack())
                itemEntity.setDefaultPickUpDelay()

                level.addFreshEntity(itemEntity)
            }
        }

        return super.playerWillDestroy(level, blockPos, blockState, player)
    }

    override fun getDrops(blockState: BlockState, builder: LootParams.Builder): MutableList<ItemStack> {
        return mutableListOf(ItemStack(Blocks.LECTERN))
    }

    override fun useWithoutItem(blockState: BlockState, level: Level, blockPos: BlockPos, player: Player, blockHitResult: BlockHitResult?): InteractionResult {
        val blockEntity = level.getBlockEntity(blockPos)
        if (blockEntity is LecternBlockEntity) {
            val itemStack = blockEntity.getItemStack()
            if (!itemStack.isEmpty && itemStack.item is PokedexItem) {
                if (!level.isClientSide) {
                    if (player.isCrouching) takeStoredItem(blockEntity, blockState, level, blockPos, player)
                    else PokedexUIPacket((blockEntity.getItemStack().item as PokedexItem).type).sendToPlayer(player as ServerPlayer)
                }
                else if (!player.isCrouching) player.playSound(CobblemonSounds.POKEDEX_OPEN)
            }
        }
        return InteractionResult.CONSUME
    }

    override fun isPathfindable(blockState: BlockState?, pathComputationType: PathComputationType?) = false

    private fun takeStoredItem(blockEntity: LecternBlockEntity, blockState: BlockState, level: Level, blockPos: BlockPos, player: Player) {
        if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty) {
            player.setItemInHand(InteractionHand.MAIN_HAND, blockEntity.removeItemStack())
            blockEntity.setRemoved()
            val facing = blockState.getValue(HorizontalDirectionalBlock.FACING)
            val newBlockState = Blocks.LECTERN.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing)
            level.setBlockAndUpdate(blockPos, newBlockState)
            level.playSound(null, blockPos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.7F, 1.0F);
        }
    }
}