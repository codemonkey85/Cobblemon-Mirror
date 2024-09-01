/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.api.apricorn.Apricorn
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.farming.ApricornHarvestEvent
import com.cobblemon.mod.common.api.tags.CobblemonBlockTags
import com.cobblemon.mod.common.api.tags.CobblemonItemTags
import com.cobblemon.mod.common.util.playSoundServer
import com.cobblemon.mod.common.util.toVec3d
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.BonemealableBlock
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.EntityCollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

// Note we cannot make this inherit from CocoaBlock since our age properties differ, it is however safe to copy most of the logic from it
@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
class ApricornBlock(settings: Properties, val apricorn: Apricorn) : HorizontalDirectionalBlock(settings), BonemealableBlock, ShearableBlock {

    init {
        registerDefaultState(stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(AGE, MIN_AGE))
    }

    override fun isRandomlyTicking(state: BlockState) = state.getValue(AGE) < MAX_AGE

    @Deprecated("Deprecated in Java")
    override fun randomTick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
        // Cocoa block uses a 5 here might as well stay consistent
        if (world.random.nextInt(5) == 0) {
            val currentAge = state.getValue(AGE)
            if (currentAge < MAX_AGE) {
                world.setBlock(pos, state.setValue(AGE, currentAge + 1), 2)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun canSurvive(state: BlockState, world: LevelReader, pos: BlockPos): Boolean {
        val blockState = world.getBlockState(pos.relative(state.getValue(FACING) as Direction))
        return blockState.`is`(CobblemonBlockTags.APRICORN_LEAVES)
    }

    override fun getShape(state: BlockState, world: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        val age = state.getValue(AGE)
        return when (state.getValue(FACING)) {
            Direction.NORTH -> NORTH_AABB[age]
            Direction.EAST -> EAST_AABB[age]
            Direction.SOUTH -> SOUTH_AABB[age]
            Direction.WEST -> WEST_AABB[age]
            else -> NORTH_AABB[age]
        }
    }

    @Deprecated("Deprecated in Java")
    override fun getCollisionShape(
        state: BlockState,
        world: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape {
        if (context is EntityCollisionContext && (context.entity as? ItemEntity)?.item?.`is`(CobblemonItemTags.APRICORNS) == true) {
            return Shapes.empty()
        }
        return super.getCollisionShape(state, world, pos, context)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? {
        var blockState = defaultBlockState()
        val worldView = ctx.level
        val blockPos = ctx.clickedPos
        ctx.nearestLookingDirections.forEach { direction ->
            if (direction.axis.isHorizontal) {
                blockState = blockState.setValue(FACING, direction) as BlockState
                if (blockState.canSurvive(worldView, blockPos)) {
                    return blockState
                }
            }
        }
        return null
    }

    override fun updateShape(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        world: LevelAccessor,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState {
        return if (direction == state.getValue(FACING) && !state.canSurvive(world, pos)) Blocks.AIR.defaultBlockState()
            else super.updateShape(state, direction, neighborState, world, pos, neighborPos)
    }

    override fun isValidBonemealTarget(world: LevelReader, pos: BlockPos, state: BlockState) = state.getValue(AGE) < MAX_AGE

    override fun isBonemealSuccess(world: Level, random: RandomSource, pos: BlockPos, state: BlockState) = true

    override fun performBonemeal(world: ServerLevel, random: RandomSource, pos: BlockPos, state: BlockState) {
        world.setBlock(pos, state.setValue(AGE, state.getValue(AGE) + 1), 2)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING, AGE)
    }

    override fun isPathfindable(state: BlockState, type: PathComputationType): Boolean = false

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        blockHitResult: BlockHitResult
    ): InteractionResult {
        if (state.getValue(AGE) != MAX_AGE) {
            return super.useWithoutItem(state, level, pos, player, blockHitResult)
        }

        doHarvest(level, state, pos, player)
        return InteractionResult.SUCCESS
    }

    override fun attack(state: BlockState, world: Level, pos: BlockPos, player: Player) {
        if (state.getValue(AGE) != MAX_AGE) {
            return super.attack(state, world, pos, player)
        }

        doHarvest(world, state, pos, player)
    }

    // We need to point back to the actual apricorn item, see SweetBerryBushBlock for example
    override fun getCloneItemStack(world: LevelReader, pos: BlockPos, state: BlockState) = ItemStack(this.apricorn.item())

    private fun doHarvest(world: Level, state: BlockState, pos: BlockPos, player: Player) {
        val resetState = this.harvest(world, state, pos)
        world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, resetState))

        if (!world.isClientSide) {
            world.playSoundServer(position = pos.toVec3d(), sound = SoundEvents.ITEM_PICKUP, volume = 0.7F, pitch = 1.4F)

            if (world is ServerLevel && player is ServerPlayer) {
                CobblemonEvents.APRICORN_HARVESTED.post(ApricornHarvestEvent(player, apricorn, world, pos))
            }
        }
    }

    /**
     * Harvests the apricorn at the given params.
     * This uses [Block.dropResources] to handle the drops.
     * It will also reset the [BlockState] of this block at the given location to the start of growth.
     *
     * @param world The [World] the apricorn is in.
     * @param state The [BlockState] of the apricorn.
     * @param pos The [BlockPos] of the apricorn.
     * @return The [BlockState] after harvest.
     */
    fun harvest(world: Level, state: BlockState, pos: BlockPos): BlockState {
        // Uses loot tables, to change the drops use 'data/cobblemon/loot_tables/blocks/<color>_apricorn.json'
        Block.dropResources(state, world, pos)
        // Don't use default as we want to keep the facing
        val resetState = state.setValue(AGE, MIN_AGE)
        world.setBlock(pos, resetState, Block.UPDATE_CLIENTS)
        return resetState
    }

    override fun attemptShear(world: Level, state: BlockState, pos: BlockPos, successCallback: () -> Unit): Boolean {
        if (state.getValue(AGE) != MAX_AGE) {
            return false
        }
        world.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1F, 1F)
        this.harvest(world, state, pos)
        successCallback()
        world.gameEvent(null, GameEvent.SHEAR, pos)
        return true
    }

    override fun codec(): MapCodec<out HorizontalDirectionalBlock> {
        return CODEC
    }

    companion object {
        val CODEC: MapCodec<ApricornBlock> = RecordCodecBuilder.mapCodec { it.group(
            propertiesCodec(),
            Apricorn.CODEC.fieldOf("apricorn").forGetter(ApricornBlock::apricorn)
        ).apply(it, ::ApricornBlock) }

        val AGE = BlockStateProperties.AGE_3
        const val MAX_AGE = BlockStateProperties.MAX_AGE_3
        const val MIN_AGE = 0

        // North
        // Stage 1
        private val NORTH_TOP_STAGE_1 = Block.box(7.0, 11.0, 0.5, 9.0, 11.5, 2.5)
        private val NORTH_BODY_STAGE_1 = Block.box(6.5, 9.0, 0.0, 9.5, 11.0, 3.0)
        private val NORTH_BOTTOM_STAGE_1 = Block.box(7.0, 8.5, 0.5, 9.0, 9.0, 2.5)

        // Stage 2
        private val NORTH_TOP_STAGE_2 = Block.box(6.5, 10.5, 0.5, 9.5, 11.0, 3.5)
        private val NORTH_BODY_STAGE_2 = Block.box(6.0, 7.5, 0.0, 10.0, 10.5, 4.0)
        private val NORTH_BOTTOM_STAGE_2 = Block.box(6.5, 7.0, 0.5, 9.5, 7.5, 3.5)

        // Stage 3
        private val NORTH_TOP_STAGE_3 = Block.box(6.0, 9.75, 0.5, 10.0, 10.5, 4.5)
        private val NORTH_BODY_STAGE_3 = Block.box(5.5, 5.75, 0.0, 10.5, 9.75, 5.0)
        private val NORTH_BOTTOM_STAGE_3 = Block.box(6.0, 5.0, 0.5, 10.0, 5.75, 4.5)

        // Full fruit
        private val NORTH_TOP_FRUIT = Block.box(6.0, 9.0, 1.0, 10.0, 10.0, 5.0)
        private val NORTH_BODY_FRUIT = Block.box(5.0, 4.0, 0.0, 11.0, 9.0, 6.0)
        private val NORTH_BOTTOM_FRUIT = Block.box(5.5, 3.0, 0.5, 10.5, 4.0, 5.5)

        private val NORTH_AABB = arrayOf(
            Shapes.or(NORTH_BODY_STAGE_1, NORTH_TOP_STAGE_1, NORTH_BOTTOM_STAGE_1),
            Shapes.or(NORTH_BODY_STAGE_2, NORTH_TOP_STAGE_2, NORTH_BOTTOM_STAGE_2),
            Shapes.or(NORTH_BODY_STAGE_3, NORTH_TOP_STAGE_3, NORTH_BOTTOM_STAGE_3),
            Shapes.or(NORTH_BODY_FRUIT, NORTH_TOP_FRUIT, NORTH_BOTTOM_FRUIT)
        )

        // South
        // Stage 1
        private val SOUTH_TOP_STAGE_1 = Block.box(7.0, 11.0, 13.5, 9.0, 11.5, 15.5)
        private val SOUTH_BODY_STAGE_1 = Block.box(6.5, 9.0, 13.0, 9.5, 11.0, 16.0)
        private val SOUTH_BOTTOM_STAGE_1 = Block.box(7.0, 8.5, 13.5, 9.0, 9.0, 15.5)

        // Stage 2
        private val SOUTH_TOP_STAGE_2 = Block.box(6.5, 10.5, 12.5, 9.5, 11.0, 15.5)
        private val SOUTH_BODY_STAGE_2 = Block.box(6.0, 7.5, 12.0, 10.0, 10.5, 16.0)
        private val SOUTH_BOTTOM_STAGE_2 = Block.box(6.5, 7.0, 12.5, 9.5, 7.5, 15.5)

        // Stage 3
        private val SOUTH_TOP_STAGE_3 = Block.box(6.0, 9.75, 11.5, 10.0, 10.5, 15.5)
        private val SOUTH_BODY_STAGE_3 = Block.box(5.5, 5.75, 11.0, 10.5, 9.75, 16.0)
        private val SOUTH_BOTTOM_STAGE_3 = Block.box(6.0, 5.0, 11.5, 10.0, 5.75, 15.5)

        // Full fruit
        private val SOUTH_TOP_FRUIT = Block.box(6.0, 9.0, 11.0, 10.0, 10.0, 15.0)
        private val SOUTH_BODY_FRUIT = Block.box(5.0, 4.0, 10.0, 11.0, 9.0, 16.0)
        private val SOUTH_BOTTOM_FRUIT = Block.box(5.5, 3.0, 10.5, 10.5, 4.0, 15.5)

        private val SOUTH_AABB = arrayOf(
            Shapes.or(SOUTH_BODY_STAGE_1, SOUTH_TOP_STAGE_1, SOUTH_BOTTOM_STAGE_1),
            Shapes.or(SOUTH_BODY_STAGE_2, SOUTH_TOP_STAGE_2, SOUTH_BOTTOM_STAGE_2),
            Shapes.or(SOUTH_BODY_STAGE_3, SOUTH_TOP_STAGE_3, SOUTH_BOTTOM_STAGE_3),
            Shapes.or(SOUTH_BODY_FRUIT, SOUTH_TOP_FRUIT, SOUTH_BOTTOM_FRUIT)
        )

        // East
        // Stage 1
        private val EAST_TOP_STAGE_1 = Block.box(13.5, 11.0, 7.0, 15.5, 11.5, 9.0)
        private val EAST_BODY_STAGE_1 = Block.box(13.0, 9.0, 6.5, 16.0, 11.0, 9.5)
        private val EAST_BOTTOM_STAGE_1 = Block.box(13.5, 8.5, 7.0, 15.5, 9.0, 9.0)

        // Stage 2
        private val EAST_TOP_STAGE_2 = Block.box(12.5, 10.5, 6.5, 15.5, 11.0, 9.5)
        private val EAST_BODY_STAGE_2 = Block.box(12.0, 7.5, 6.0, 16.0, 10.5, 10.0)
        private val EAST_BOTTOM_STAGE_2 = Block.box(12.5, 7.0, 6.5, 15.5, 7.5, 9.5)

        // Stage 3
        private val EAST_TOP_STAGE_3 = Block.box(11.5, 9.75, 6.0, 15.5, 10.5, 10.0)
        private val EAST_BODY_STAGE_3 = Block.box(11.0, 5.75, 5.5, 16.0, 9.75, 10.5)
        private val EAST_BOTTOM_STAGE_3 = Block.box(11.5, 5.0, 6.0, 15.5, 5.75, 10.0)

        // Full fruit
        private val EAST_TOP_FRUIT = Block.box(11.0, 9.0, 6.0, 15.0, 10.0, 10.0)
        private val EAST_BODY_FRUIT = Block.box(10.0, 4.0, 5.0, 16.0, 9.0, 11.0)
        private val EAST_BOTTOM_FRUIT = Block.box(10.5, 3.0, 5.5, 15.5, 4.0, 10.5)

        private val EAST_AABB = arrayOf(
            Shapes.or(EAST_BODY_STAGE_1, EAST_TOP_STAGE_1, EAST_BOTTOM_STAGE_1),
            Shapes.or(EAST_BODY_STAGE_2, EAST_TOP_STAGE_2, EAST_BOTTOM_STAGE_2),
            Shapes.or(EAST_BODY_STAGE_3, EAST_TOP_STAGE_3, EAST_BOTTOM_STAGE_3),
            Shapes.or(EAST_BODY_FRUIT, EAST_TOP_FRUIT, EAST_BOTTOM_FRUIT)
        )

        // West
        // Stage 1
        private val WEST_TOP_STAGE_1 = Block.box(0.5, 11.0, 7.0, 2.5, 11.5, 9.0)
        private val WEST_BODY_STAGE_1 = Block.box(0.0, 9.0, 6.5, 3.0, 11.0, 9.5)
        private val WEST_BOTTOM_STAGE_1 = Block.box(0.5, 8.5, 7.0, 2.5, 9.0, 9.0)

        // Stage 2
        private val WEST_TOP_STAGE_2 = Block.box(0.5, 10.5, 6.5, 3.5, 11.0, 9.5)
        private val WEST_BODY_STAGE_2 = Block.box(0.0, 7.5, 6.0, 4.0, 10.5, 10.0)
        private val WEST_BOTTOM_STAGE_2 = Block.box(0.5, 7.0, 6.5, 3.5, 7.5, 9.5)

        // Stage 3
        private val WEST_TOP_STAGE_3 = Block.box(0.5, 9.75, 6.0, 4.5, 10.5, 10.0)
        private val WEST_BODY_STAGE_3 = Block.box(0.0, 5.75, 5.5, 5.0, 9.75, 10.5)
        private val WEST_BOTTOM_STAGE_3 = Block.box(0.5, 5.0, 6.0, 4.5, 5.75, 10.0)

        // Full fruit
        private val WEST_TOP_FRUIT = Block.box(1.0, 9.0, 6.0, 5.0, 10.0, 10.0)
        private val WEST_BODY_FRUIT = Block.box(0.0, 4.0, 5.0, 6.0, 9.0, 11.0)
        private val WEST_BOTTOM_FRUIT = Block.box(0.5, 3.0, 5.5, 5.5, 4.0, 10.5)

        private val WEST_AABB = arrayOf(
            Shapes.or(WEST_BODY_STAGE_1, WEST_TOP_STAGE_1, WEST_BOTTOM_STAGE_1),
            Shapes.or(WEST_BODY_STAGE_2, WEST_TOP_STAGE_2, WEST_BOTTOM_STAGE_2),
            Shapes.or(WEST_BODY_STAGE_3, WEST_TOP_STAGE_3, WEST_BOTTOM_STAGE_3),
            Shapes.or(WEST_BODY_FRUIT, WEST_TOP_FRUIT, WEST_BOTTOM_FRUIT)
        )

    }


}