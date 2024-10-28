/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.block.entity.DisplayCaseBlockEntity
import net.minecraft.block.*
import net.minecraft.block.HorizontalFacingBlock.*
import net.minecraft.entity.ai.pathing.NavigationType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.Items
import net.minecraft.particle.ParticleTypes
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.DirectionProperty
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess

@Suppress("OVERRIDE_DEPRECATION")
class SweetIncenseBlock(settings: Settings) : BlockWithEntity(settings), Waterloggable {
    /*
    This item, when placed, acts like a candle. It must be lit with a Flint n Steel or Fire Charge, and it can be “put out” by right-clicking on it. Small blue smoke particles will rise from the nub on the top of the block when lit.

    This incense block has special effects on Pokémon spawns, but only when lit. When lit, Sweet Incense increases Pokémon spawn & despawn rates by 1.5x and raises spawn density by 25% in a 16 block radius (32 block diameter) (this number will need to be playtested).

    This essentially means that the player will see a larger number of Pokémon at once, and a higher variety of Pokémon in a given span of time.

     */

    init {
        this.defaultState = this.stateManager.defaultState
            .with(FACING, Direction.NORTH)
            .with(ITEM_DIRECTION, Direction.NORTH)
            .with(LIT,false)
    }

    companion object {
        val ITEM_DIRECTION = DirectionProperty.of("item_facing")
        val LIT = BooleanProperty.of("false")
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        var blockState = defaultState
        val worldView = ctx.world
        val blockPos = ctx.blockPos
        ctx.placementDirections.forEach { direction ->
            if (direction.axis.isHorizontal) {
                blockState = blockState
                    .with(FACING, direction)
                    .with(LIT, false)
                    .with(ITEM_DIRECTION, direction)
                        as BlockState

                if (blockState.canPlaceAt(worldView, blockPos)) {
                    return blockState
                }
            }
        }
        return null
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(FACING)
        builder.add(ITEM_DIRECTION)
        builder.add(LIT)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): DisplayCaseBlockEntity {
        return DisplayCaseBlockEntity(pos, state)
    }

    override fun getStateForNeighborUpdate(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        world: WorldAccess,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState {
        return if (direction == state.get(FACING) && !state.canPlaceAt(world, pos)) Blocks.AIR.defaultState
        else super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos)
    }

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult
    ): ActionResult {
        // light the incense when flint and steel or fire charge is held
        if (state.get(LIT) == false && (player.getStackInHand(hand).isOf(Items.FLINT_AND_STEEL.asItem()) ||
            player.getStackInHand(hand).isOf(Items.FIRE_CHARGE.asItem()))) {
            world.setBlockState(pos, state.with(LIT, true), 2)
            return ActionResult.SUCCESS
        }

        // unlight the incense when used if it is on
        if (state.get(LIT) == true && (player.getStackInHand(hand).isOf(Items.FLINT_AND_STEEL.asItem()) == false ||
            player.getStackInHand(hand).isOf(Items.FIRE_CHARGE.asItem()) == false)) {
            world.setBlockState(pos, state.with(LIT, false), 2)
            return ActionResult.SUCCESS
        }

        return ActionResult.FAIL
    }

    override fun randomDisplayTick(state: BlockState, world: World, pos: BlockPos, random: Random) {
        if (state.get(LIT)) {
            spawnSmokeParticle(world, pos, true, false)

            if (random.nextInt(10) == 0) {
                world.playSound(
                    pos.x.toDouble() + 0.5,
                    pos.y.toDouble() + 0.5,
                    pos.z.toDouble() + 0.5,
                    SoundEvents.BLOCK_CAMPFIRE_CRACKLE,
                    SoundCategory.BLOCKS,
                    0.2f + random.nextFloat(),
                    random.nextFloat() * 0.7f + 0.6f,
                    false
                )
            }

            /*if (random.nextInt(5) == 0) {
                for (i in 0 until random.nextInt(1) + 1) {
                    world.addParticle(
                        ParticleTypes.LAVA, pos.x.toDouble() + 0.5, pos.y.toDouble() + 0.5, pos.z.toDouble() + 0.5,
                        (random.nextFloat() / 2.0f).toDouble(), 5.0E-5,
                        (random.nextFloat() / 2.0f).toDouble()
                    )
                }
            }*/
        }
    }

    fun spawnSmokeParticle(world: World, pos: BlockPos, isSignal: Boolean, lotsOfSmoke: Boolean) {
        val random = world.getRandom()
        val defaultParticleType =
            if (isSignal) ParticleTypes.CAMPFIRE_SIGNAL_SMOKE else ParticleTypes.CAMPFIRE_COSY_SMOKE
        world.addImportantParticle(
            defaultParticleType,
            true,
            pos.x.toDouble() + 0.5 + random.nextDouble() / 3.0 * (if (random.nextBoolean()) 1 else -1).toDouble(),
            pos.y.toDouble() + random.nextDouble() + random.nextDouble(),
            pos.z.toDouble() + 0.5 + random.nextDouble() / 3.0 * (if (random.nextBoolean()) 1 else -1).toDouble(),
            0.0,
            0.07,
            0.0
        )
        if (lotsOfSmoke) {
            world.addParticle(
                ParticleTypes.SMOKE,
                pos.x.toDouble() + 0.5 + random.nextDouble() / 4.0 * (if (random.nextBoolean()) 1 else -1).toDouble(),
                pos.y.toDouble() + 0.4,
                pos.z.toDouble() + 0.5 + random.nextDouble() / 4.0 * (if (random.nextBoolean()) 1 else -1).toDouble(),
                0.0,
                0.005,
                0.0
            )
        }
    }

    override fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity) {
        /*val entity = world.getBlockEntity(pos) as DisplayCaseBlockEntity
        if (!entity.getStack().isEmpty && !player.isCreative) {
            ItemScatterer.spawn(world, pos, entity.inv)
        }
        super.onBreak(world, pos, state, player)*/
    }

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

    override fun hasComparatorOutput(state: BlockState?) = true

    override fun canPathfindThrough(state: BlockState, world: BlockView, pos: BlockPos, type: NavigationType) = false

}