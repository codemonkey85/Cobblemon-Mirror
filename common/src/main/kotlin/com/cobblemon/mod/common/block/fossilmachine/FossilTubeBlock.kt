/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.fossilmachine

import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.api.multiblock.MultiblockBlock
import com.cobblemon.mod.common.api.multiblock.MultiblockEntity
import com.cobblemon.mod.common.block.entity.fossil.FossilMultiblockEntity
import com.cobblemon.mod.common.block.entity.fossil.FossilTubeBlockEntity
import com.cobblemon.mod.common.block.multiblock.FossilMultiblockBuilder
import net.minecraft.block.*
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.EnumProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldView


class FossilTubeBlock(properties: Settings) : MultiblockBlock(properties), InventoryProvider {
    init {
        defaultState = defaultState
            .with(HorizontalFacingBlock.FACING, Direction.NORTH)
            .with(PART, TubePart.BOTTOM)
            .with(TRIGGERED, false)
    }


    fun getPositionOfOtherPart(state: BlockState, pos: BlockPos): BlockPos {
        return if (state.get(PART) == TubePart.BOTTOM) {
            pos.up()
        } else {
            pos.down()
        }
    }

    fun getBasePosition(state: BlockState, pos: BlockPos): BlockPos {
        return if (isBase(state)) {
            pos
        } else {
            pos.down()
        }
    }

    private fun isBase(state: BlockState): Boolean = state.get(PART) == TubePart.BOTTOM

    override fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity?) {
        super.onBreak(world, pos, state, player)
        val otherPart = world.getBlockState(getPositionOfOtherPart(state, pos))
        if (otherPart.block is FossilTubeBlock) {
            world.setBlockState(getPositionOfOtherPart(state, pos), Blocks.AIR.defaultState, 35)
            world.syncWorldEvent(player, 2001, getPositionOfOtherPart(state, pos), getRawIdFromState(otherPart))
        }
    }

    override fun onPlaced(
        world: World,
        pos: BlockPos,
        state: BlockState,
        placer: LivingEntity?,
        itemStack: ItemStack?
    ) {
        //Place the full block before we call to super to validate the multiblock
        world.setBlockState(pos.up(), state.with(PART, TubePart.TOP) as BlockState, 3)
        world.updateNeighbors(pos, Blocks.AIR)
        state.updateNeighbors(world, pos, 3)
        super.onPlaced(world, pos, state, placer, itemStack)
    }

    @Deprecated("Deprecated in Java")
    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        if(state.get(PART) == TubePart.TOP) {
            // TubeTop isn't reliably up to date on clients, need to look at the bottom half
            val tubeBottomPos = pos.down()
            val tubeBottomState = world.getBlockState(tubeBottomPos)
            if(tubeBottomState.block.equals(CobblemonBlocks.FOSSIL_TUBE.asBlock()) && tubeBottomState.get(PART) == TubePart.BOTTOM) {
                return super.onUse(tubeBottomState, world, tubeBottomPos, player, hand, hit)
            }
        }
        return super.onUse(state, world, pos, player, hand, hit)
    }

    override fun createMultiBlockEntity(pos: BlockPos, state: BlockState): FossilMultiblockEntity {
        return if (state.get(PART) == TubePart.BOTTOM) {
            FossilTubeBlockEntity(pos, state, FossilMultiblockBuilder(pos))
        } else {
            FossilMultiblockEntity(pos, state, FossilMultiblockBuilder(pos))
        }
    }

    override fun getPlacementState(blockPlaceContext: ItemPlacementContext): BlockState? {
        val abovePosition = blockPlaceContext.blockPos.up()
        val world = blockPlaceContext.world
        if (world.getBlockState(abovePosition).canReplace(blockPlaceContext) && !world.isOutOfHeightLimit(abovePosition)) {
            return defaultState
                .with(HorizontalFacingBlock.FACING, blockPlaceContext.horizontalPlayerFacing)
                .with(PART, TubePart.BOTTOM)
        }

        return null
    }

    override fun canPlaceAt(state: BlockState, world: WorldView, pos: BlockPos): Boolean {
        val blockPos = pos.down()
        val blockState = world.getBlockState(blockPos)
        return if (state.get(PART) == TubePart.BOTTOM) blockState.isSideSolidFullSquare(world, blockPos, Direction.UP) else blockState.isOf(this)
    }
    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(HorizontalFacingBlock.FACING)
        builder.add(PART)
        builder.add(TRIGGERED)

    }

    @Deprecated("Deprecated in Java")
    override fun hasComparatorOutput(state: BlockState?): Boolean {
        // TODO: return false if not attached to a multiblock structure
        return true
    }

    @Deprecated("Deprecated in Java")
    override fun getComparatorOutput(state: BlockState, world: World?, pos: BlockPos?): Int {
        if(world == null || pos == null) {
            return 0
        }
        val tubeEntity = world.getBlockEntity(pos) as MultiblockEntity
        val multiBlockEntity = tubeEntity.multiblockStructure
        if(multiBlockEntity != null) {
            return multiBlockEntity.getComparatorOutput(state, world, pos)
        }
        return 0
    }

    @Deprecated("Deprecated in Java")
    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos?, newState: BlockState, moved: Boolean) {
        if (!state.isOf(newState.block)) super.onStateReplaced(state, world, pos, newState, moved)
    }

    @Deprecated("Deprecated in Java")
    override fun neighborUpdate(state: BlockState, world: World, pos: BlockPos, sourceBlock: Block?, sourcePos: BlockPos?, notify: Boolean) {
        val bl = world.isReceivingRedstonePower(pos) || world.isReceivingRedstonePower(pos.up())
        val bl2 = state.get(TRIGGERED)
        if (bl && !bl2) {
            world.scheduleBlockTick(pos, this, 4)
            world.setBlockState(pos, state.with(TRIGGERED, true) as BlockState, NO_REDRAW)
        } else if (!bl && bl2) {
            world.setBlockState(pos, state.with(TRIGGERED, false) as BlockState, NO_REDRAW)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun scheduledTick(state: BlockState?, world: ServerWorld?, pos: BlockPos?, random: Random?) {
        if(world == null || pos == null) {
            return
        }
        val tubeEntity = world.getBlockEntity(pos) as MultiblockEntity
        if (tubeEntity.multiblockStructure != null) {
            tubeEntity.multiblockStructure!!.onTriggerEvent(state, world, pos, random)
        }
    }

    override fun getOutlineShape(
        state: BlockState,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape {
        return if (state.get(PART) == TubePart.TOP) {
            var shape = VoxelShapes.cuboid(0.0625, 0.0, 0.0625, 0.9375, 0.8125, 0.9375)
            shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.0, 0.8125, 0.0, 1.0, 1.0, 1.0))
            shape
        } else {
            var shape = VoxelShapes.cuboid(0.0625, 0.1875, 0.0625, 0.9375, 1.0, 0.9375)
            shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 0.1875, 1.0))
            shape
        }
    }
    override fun getInventory(
        state: BlockState,
        world: WorldAccess,
        pos: BlockPos
    ): SidedInventory {
        val tubeEntity =
            (if (state.get(PART) == TubePart.TOP) world.getBlockEntity(pos.down())
            else world.getBlockEntity(pos)) as FossilTubeBlockEntity

        return tubeEntity.inv
    }

    enum class TubePart(private val label: String) : StringIdentifiable {
        TOP("top"),
        BOTTOM("bottom");
        override fun asString() = label
    }

    companion object {
        val PART = EnumProperty.of("part", TubePart::class.java)
        val TRIGGERED = Properties.TRIGGERED
    }
}
