/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.api.tags.CobblemonItemTags
import com.cobblemon.mod.common.block.SaccharineStrippedLogBlock.Companion
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.BlockTags
import net.minecraft.util.*
import net.minecraft.world.*
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.BonemealableBlock
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.EntityCollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import javax.swing.text.html.BlockView
import kotlin.random.Random

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
class SaccharineLeafBlock(settings: Properties) : LeavesBlock(settings), BonemealableBlock {

    init {
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(AGE, MIN_AGE)
                        .setValue(DISTANCE, DISTANCE_MAX)
                        .setValue(PERSISTENT, false)
                        .setValue(WATERLOGGED, false)
        )
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState {
        val level = ctx.level
        val pos = ctx.clickedPos
        val fluidState = level.getFluidState(pos)
        return this.defaultBlockState()
                .setValue(PERSISTENT, true)
                .setValue(WATERLOGGED, fluidState.`is`(Fluids.WATER))
    }

    override fun updateShape(state: BlockState, direction: Direction, neighborState: BlockState, level: LevelAccessor, pos: BlockPos, neighborPos: BlockPos): BlockState {
        return if (!state.canSurvive(level, pos)) {
            Blocks.AIR.defaultBlockState()
        } else {
            super.updateShape(state, direction, neighborState, level, pos, neighborPos)
        }
    }

    override fun isRandomlyTicking(state: BlockState) = state.getValue(AGE) != 0

    override fun randomTick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {

        if (random.nextInt(2) == 0) {


            for (i in 1..10) {

                val belowPos = pos.below(i)
                val belowState = world.getBlockState(belowPos)
                val currentAge = state.getValue(AGE)

                if (belowState.isAir) {
                    continue
                } else if (belowState.block is SaccharineLeafBlock) {
                    val belowAge = belowState.getValue(AGE)
                    if (belowAge < MAX_AGE) {
                        // Decrease age from the top leaf block
                        val newCurrentAge = (currentAge - 1).coerceAtLeast(MIN_AGE)
                        world.setBlock(pos, state.setValue(AGE, newCurrentAge), 2)

                        // Increase age from the bottom leaf block
                        val newBelowAge = (belowAge + 1).coerceAtMost(MAX_AGE)
                        world.setBlock(belowPos, belowState.setValue(AGE, newBelowAge), 2)
                    }
                    break
                } else {
                    break
                }
            }
        }

        super.randomTick(state,  world, pos, random)
    }


    @Deprecated("Deprecated in Java")
    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return SHAPE
    }
    // todo make block
    @Deprecated("Deprecated in Java")
    override fun getCollisionShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        if (context is EntityCollisionContext && (context.entity as? ItemEntity)?.item?.`is`(CobblemonItemTags.APRICORNS) == true) {
            return Shapes.empty()
        }
        return super.getCollisionShape(state, level, pos, context)
    }

    override fun isValidBonemealTarget(level: LevelReader, pos: BlockPos, state: BlockState): Boolean = true

    override fun isBonemealSuccess(level: Level, random: RandomSource, pos: BlockPos, state: BlockState) = true

    override fun performBonemeal(level: ServerLevel, random: RandomSource, pos: BlockPos, state: BlockState) {
        level.setBlock(pos, state.setValue(AGE, state.getValue(AGE) + 1), 2)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(AGE)
    }

    @Deprecated("Deprecated in Java")
    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {

        if (state.getValue(AGE) == 2) {
            for (i in 0 until random.nextInt(1) + 1) {
                this.spawnHoneyParticles(level, pos, state, .75f)
            }
        } else if (state.getValue(AGE) == 1) {
            for (i in 0 until random.nextInt(1) + 1) {
                this.spawnHoneyParticles(level, pos, state, .9f)
            }
        }

        // this code was for them aging as time goes on
        /* if (world.random.nextInt(5) == 0) {

            val currentAge = state.get(AGE)
            if (currentAge < MAX_AGE) {
                world.setBlockState(pos, state.setValue(AGE, currentAge + 1), 2)
            }
        }*/
    }

    private fun spawnHoneyParticles(level: Level, pos: BlockPos, state: BlockState, rate: Float) {
        if (state.fluidState.isEmpty && !(level.random.nextFloat() < rate)) {
            val voxelShape = state.getCollisionShape(level, pos)
            val d = voxelShape.max(Direction.Axis.Y)
            if (d >= 1.0 && !state.`is`(BlockTags.IMPERMEABLE)) {
                val e = voxelShape.min(Direction.Axis.Y)
                if (e > 0.0) {
                    this.addHoneyParticle(level, pos, voxelShape, pos.y.toDouble() + e - 0.05)
                } else {
                    val blockPos = pos.below()
                    val blockState = level.getBlockState(blockPos)
                    val voxelShape2 = blockState.getCollisionShape(level, blockPos)
                    val f = voxelShape2.max(Direction.Axis.Y)
                    if ((f < 1.0 || !blockState.isSolidRender(level, blockPos)) && blockState.fluidState.isEmpty) {
                        this.addHoneyParticle(level, pos, voxelShape, pos.y.toDouble() - 0.05)
                    }
                }
            }
        }
    }

    private fun addHoneyParticle(level: Level, pos: BlockPos, shape: VoxelShape, height: Double) {
        this.addHoneyParticle(
                level, pos.x.toDouble() + shape.min(Direction.Axis.X), pos.x.toDouble() + shape.max(
                Direction.Axis.X
            ), pos.z.toDouble() + shape.min(Direction.Axis.Z), pos.z.toDouble() + shape.max(
                Direction.Axis.Z
            ), height
        )
    }

    private fun addHoneyParticle(level: Level, minX: Double, maxX: Double, minZ: Double, maxZ: Double, height: Double) {
        level.addParticle(
            ParticleTypes.DRIPPING_HONEY,
            Mth.lerp(level.random.nextDouble(), minX, maxX),
            height,
            Mth.lerp(level.random.nextDouble(), minZ, maxZ),
            0.0,
            0.0,
            0.0
        )
    }

    override fun isPathfindable(state: BlockState, type: PathComputationType): Boolean = false

    override fun useItemOn(stack: ItemStack, state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hit: BlockHitResult): ItemInteractionResult {
        // todo if item in players hand is glass bottle and AGE is more than 0
        val itemStack = player.getItemInHand(hand)
        if (itemStack.`is`(Items.GLASS_BOTTLE) && state.getValue(AGE) > 0)
        {
            // decrement stack if not in creative mode
            if (!player.isCreative)
                itemStack.shrink(1)

            // give player honey bottle for now
            player.addItem(Items.HONEY_BOTTLE.defaultInstance)

            // todo reset AGE
            level.setBlock(pos, state.setValue(AGE, 0), 2)

            val currentAge = state.getValue(AGE)
        } else if (itemStack.`is`(Items.HONEY_BOTTLE) && state.getValue(AGE) != 2) {
            // decrement stack if not in creative mode
            if (!player.isCreative)
                itemStack.shrink(1)

            // todo set age to 2
            level.setBlock(pos, state.setValue(AGE, 2), 2)
        }

        if (state.getValue(AGE) != MAX_AGE) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit)
        }

        //doHarvest(world, state, pos, player)
        return ItemInteractionResult.SUCCESS
    }

    /*override fun onBlockBreakStart(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity) {
        if (state.get(AGE) != MAX_AGE) {
            return super.onBlockBreakStart(state, world, pos, player)
        }

        //doHarvest(world, state, pos, player)
    }*/

    companion object {

        val AGE: IntegerProperty = BlockStateProperties.AGE_2
        val DISTANCE: IntegerProperty = BlockStateProperties.DISTANCE
        val PERSISTENT: BooleanProperty = BlockStateProperties.PERSISTENT
        const val MAX_AGE = 2
        const val MIN_AGE = 0
        const val DISTANCE_MAX = 7

        private val SHAPE: VoxelShape = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
    }

}
