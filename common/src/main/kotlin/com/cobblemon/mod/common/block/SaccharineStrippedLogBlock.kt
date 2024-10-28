/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.tags.CobblemonItemTags
import net.minecraft.block.*
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.pathing.NavigationType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.EnumProperty
import net.minecraft.state.property.IntProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.Axis
import net.minecraft.util.math.random.Random
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
class SaccharineStrippedLogBlock(settings: Settings) : PillarBlock(settings) {

    init {
        this.defaultState = this.stateManager.defaultState
            .with(AGE, MIN_AGE)
            .with(AXIS, Axis.Y)
    }

    override fun onPlaced(world: World?, pos: BlockPos?, state: BlockState?, placer: LivingEntity?, itemStack: ItemStack?) {
        owner = placer
        super.onPlaced(world, pos, state, placer, itemStack)
    }

    override fun hasRandomTicks(state: BlockState) = state.get(AGE) < MAX_AGE

    @Deprecated("Deprecated in Java")
    override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {

        // todo make it so that it only ages when connected to enough natural-tagged Saccharine Leaves (so, generated or grown, not player placed)

        // this code was for them aging as time goes on
         if (world.random.nextInt(5) == 0 && owner == null) {

            val currentAge = state.get(AGE)
            if (currentAge < MAX_AGE) {
                world.setBlockState(pos, state.with(AGE, currentAge + 1), 2)
            }
        }
    }

    /*@Deprecated("Deprecated in Java")
    override fun canPlaceAt(state: BlockState, world: WorldView, pos: BlockPos): Boolean {
        val blockState = world.getBlockState(pos.down())
        return blockState.isIn(CobblemonBlockTags.SACCHARINE_LEAVES)
    }*/

    @Deprecated("Deprecated in Java")
    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape {
        return SHAPE
    }

    // todo make block
    @Deprecated("Deprecated in Java")
    override fun getCollisionShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape {
        if (context is EntityShapeContext && (context.entity as? ItemEntity)?.stack?.isIn(CobblemonItemTags.APRICORNS) == true) {
            return VoxelShapes.empty()
        }
        return super.getCollisionShape(state, world, pos, context)
    }

    /*override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        val blockState = defaultState
        val worldView = ctx.world
        val blockPos = ctx.blockPos
        return defaultState.with(PillarBlock.AXIS, ctx.side.axis) as BlockState
        return if (blockState.canPlaceAt(worldView, blockPos)) blockState else null
    }*/

    override fun getStateForNeighborUpdate(state: BlockState, direction: Direction, neighborState: BlockState, world: WorldAccess, pos: BlockPos, neighborPos: BlockPos): BlockState? {
        return if (!state.canPlaceAt(world, pos)) Blocks.AIR.defaultState
        else super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(AGE, AXIS)
    }

    override fun canPathfindThrough(state: BlockState, world: BlockView, pos: BlockPos, type: NavigationType) = false

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        // todo if item in players hand is glass bottle and AGE is 1
        if (player.getStackInHand(hand).isOf(Items.GLASS_BOTTLE) && state.get(AGE) == 1)
        {
            // decrement stack if not in creative mode
            if (!player.isCreative)
                player.getStackInHand(hand).decrement(1)

            // todo give player Sweet Sap item
            player.giveItemStack(CobblemonItems.SWEET_SAP.defaultStack)

            // todo reset AGE
            world.setBlockState(pos, state.with(AGE, 0), 2)

            val currentAge = state.get(AGE)
        }

        if (state.get(AGE) != MAX_AGE) {
            return super.onUse(state, world, pos, player, hand, hit)
        }

        //doHarvest(world, state, pos, player)
        return ActionResult.SUCCESS
    }

    /*override fun onBlockBreakStart(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity) {
        if (state.get(AGE) != MAX_AGE) {
            return super.onBlockBreakStart(state, world, pos, player)
        }

        //doHarvest(world, state, pos, player)
    }*/

    companion object {

        val AGE: IntProperty = Properties.AGE_1
        val AXIS: EnumProperty<Axis> = Properties.AXIS
        const val MAX_AGE = Properties.AGE_1_MAX
        const val MIN_AGE = 0
        var owner: LivingEntity? = null


        private val SHAPE: VoxelShape = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
    }

}
