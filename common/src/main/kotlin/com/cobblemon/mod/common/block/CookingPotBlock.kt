/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.block.entity.CookingPotBlockEntity
import com.cobblemon.mod.common.gui.CookingPotScreenHandler
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.ai.pathing.NavigationType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.fluid.FluidState
import net.minecraft.fluid.Fluids
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.SimpleNamedScreenHandlerFactory
import net.minecraft.server.world.ServerWorld
import net.minecraft.stat.Stats
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Properties
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess

class CookingPotBlock(settings: Settings?) : BlockWithEntity(settings), Waterloggable, InventoryProvider {

    companion object {
        val COOKING: BooleanProperty = BooleanProperty.of("cooking")
        val FACING = Properties.FACING
        val TRIGGERED = Properties.TRIGGERED
        val WATERLOGGED = Properties.WATERLOGGED

        private val NORTH_OUTLINE = VoxelShapes.union(
            VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 0.3125, 0.9375),
            VoxelShapes.cuboid(0.0, 0.3125, 0.75, 1.0, 0.9375, 0.9375),
            VoxelShapes.cuboid(0.0625, 0.3125, 0.0625, 0.9375, 0.875, 0.9375)
        )

        private val SOUTH_OUTLINE = VoxelShapes.union(
            VoxelShapes.cuboid(0.0, 0.0, 0.0625, 1.0, 0.3125, 1.0),
            VoxelShapes.cuboid(0.0, 0.3125, 0.0625, 1.0, 0.9375, 0.25),
            VoxelShapes.cuboid(0.0625, 0.3125, 0.0625, 0.9375, 0.875, 0.9375)
        )

        private val WEST_OUTLINE = VoxelShapes.union(
            VoxelShapes.cuboid(0.0, 0.0, 0.0, 0.9375, 0.3125, 1.0),
            VoxelShapes.cuboid(0.75, 0.3125, 0.0, 0.9375, 0.9375, 1.0),
            VoxelShapes.cuboid(0.0625, 0.3125, 0.0625, 0.9375, 0.875, 0.9375)
        )

        private val EAST_OUTLINE = VoxelShapes.union(
            VoxelShapes.cuboid(0.0625, 0.0, 0.0, 1.0, 0.3125, 1.0),
            VoxelShapes.cuboid(0.0625, 0.3125, 0.0, 0.25, 0.9375, 1.0),
            VoxelShapes.cuboid(0.0625, 0.3125, 0.0625, 0.9375, 0.875, 0.9375)
        )

        private val EMPTY_INVENTORY = object : SidedInventory {
            override fun clear() {}

            override fun size(): Int = 0

            override fun isEmpty(): Boolean = true

            override fun getStack(slot: Int) = ItemStack.EMPTY

            override fun removeStack(slot: Int, amount: Int) = ItemStack.EMPTY

            override fun removeStack(slot: Int) = ItemStack.EMPTY

            override fun setStack(slot: Int, stack: ItemStack?) {}

            override fun canPlayerUse(player: PlayerEntity?) = false

            override fun getAvailableSlots(side: Direction?) = IntArray(0)

            override fun canInsert(slot: Int, stack: ItemStack?, dir: Direction?) = false

            override fun canExtract(slot: Int, stack: ItemStack?, dir: Direction?) = false

            override fun markDirty() {}
        }
    }

    private val TITLE: Text = Text.of("Campfire Pot")

    init {
        defaultState = this.stateManager.defaultState.with(FACING, Direction.NORTH)
            .with(WATERLOGGED, false)
            .with(COOKING, false)
            .with(TRIGGERED, false)
    }

    @Deprecated("Deprecated in Java")
    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult
    ): ActionResult {
        if (world.isClient) {
            return ActionResult.SUCCESS
        }
        player.openHandledScreen(state.createScreenHandlerFactory(world, pos))
        player.incrementStat(Stats.INTERACT_WITH_CRAFTING_TABLE)
        return ActionResult.CONSUME
    }

    @Deprecated("Deprecated in Java")
    override fun createScreenHandlerFactory(
        state: BlockState,
        world: World,
        pos: BlockPos
    ): NamedScreenHandlerFactory {
        return SimpleNamedScreenHandlerFactory({ syncId: Int, inventory: PlayerInventory, player: PlayerEntity ->
            CookingPotScreenHandler(syncId, inventory, ScreenHandlerContext.create(world, pos))
        }, TITLE)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return CookingPotBlockEntity(pos, state)
    }

    override fun getInventory(state: BlockState, world: WorldAccess, pos: BlockPos): SidedInventory {
        val cookingPotBlockEntity = world.getBlockEntity(pos) as? CookingPotBlockEntity
            ?: return EMPTY_INVENTORY
        return cookingPotBlockEntity.cookingPotInventory
    }

    @Deprecated("Deprecated in Java")
    override fun canPathfindThrough(
        blockState: BlockState,
        blockGetter: BlockView,
        blockPos: BlockPos,
        pathComputationType: NavigationType
    ) = false

    @Deprecated("Deprecated in Java")
    override fun hasComparatorOutput(state: BlockState): Boolean {
        return true
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(FACING, WATERLOGGED, COOKING, TRIGGERED)
    }

    @Deprecated("Deprecated in Java")
    override fun rotate(blockState: BlockState, rotation: BlockRotation): BlockState {
        return blockState.with(FACING, rotation.rotate(blockState[FACING]))
    }

    @Deprecated("Deprecated in Java")
    override fun mirror(blockState: BlockState, mirror: BlockMirror): BlockState {
        return blockState.rotate(mirror.getRotation(blockState[FACING]))
    }

    @Deprecated("Deprecated in Java")
    override fun onStateReplaced(
        state: BlockState,
        world: World,
        pos: BlockPos,
        newState: BlockState,
        moved: Boolean
    ) {
        if (!state.isOf(newState.block)) {
            super.onStateReplaced(state, world, pos, newState, moved)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun scheduledTick(state: BlockState?, world: ServerWorld?, pos: BlockPos?, random: net.minecraft.util.math.random.Random?) {
        val inventory = (world!!.getBlockEntity(pos) as CookingPotBlockEntity).cookingPotInventory
        val cookingPotEntity = (world.getBlockEntity(pos) as CookingPotBlockEntity)

        if(world == null || pos == null) {
            return
        }
        //val tankEntity = world.getBlockEntity(pos) as MultiblockEntity

        if (cookingPotEntity.blockState.get(COOKING)) {
            //print("TMM is in use so scheduled Tick is disabled")
            return
        }

        /*//this.onTriggerEvent(state, world, pos, random)
        // todo if the output slot is empty then try to craft a TM to that slot
        if (ItemStack.areItemsEqual(inventory.items!!.get(3), ItemStack.EMPTY)) {
            if (state?.let { getInventory(it, world, pos) } != null) {
                val itemStack: ItemStack
                itemStack = if (inventory.filterTM != null)
                    inventory.filterTM!!.copy()
                else
                    ItemStack(CobblemonItems.BLANK_TM, 1)
                val tm = TechnicalMachines.getTechnicalMachineFromStack(itemStack)
                // todo use isReadyToCraftTM  to finalize the creation
                if (((inventory.filterTM != null && tm != null) && isReadyToCraftTM(state, world, pos, tm)) || isReadyToCraftBlankTM(state, world, pos)) {

                    // create TM item to the output slot
                    inventory.items!!.set(3, itemStack)
                    cookingPotEntity.markDirty()

                    // Play sound for TM creation
                    world.playSoundServer(cookingPotEntity.blockPos.toVec3d(), CobblemonSounds.TMM_CRAFT, SoundCategory.BLOCKS)
                }
            }
        }*/
        // todo if there is an item in the output slot then spit it out
        else {
            if (state?.let { getInventory(it, world, pos) } != null) {

                val itemStack = inventory.items!!.get(3)

                // Get the direction the block is facing
                val facingDirection = state.get(Properties.FACING)?.opposite ?: return

                // Calculate the center position of the block
                val frontOffset = 0.5 // Half block offset to the front
                val spawnX = pos.x + 0.5 + facingDirection.offsetX * frontOffset
                val spawnY = pos.y + 0.3 + facingDirection.offsetY * frontOffset
                val spawnZ = pos.z + 0.5 + facingDirection.offsetZ * frontOffset

                // Create the ItemEntity at the center of the block
                val itemEntity = ItemEntity(world, spawnX, spawnY, spawnZ, itemStack).copy()

                // Create the ItemEntity
                itemEntity.setVelocity(0.0, 0.0, 0.0)

                // Add the ItemEntity to the world
                world.spawnEntity(itemEntity)

                //clear inventory of SidedInventory
                inventory.items?.clear()
                cookingPotEntity.markDirty()
                //getInventory(state, world, pos).clear()
            }
        }

        /*if (state?.let { getInventory(it, world, pos) } != null) {
            val itemStack: ItemStack
            itemStack = if (inventory.filterTM != null)
                inventory.filterTM!!
            else
                ItemStack(CobblemonItems.BLANK_TM, 1)
            val tm = TechnicalMachines.getTechnicalMachineFromStack(itemStack)
            // todo use isReadyToCraftTM  to finalize the creation
            if ((inventory.filterTM != null && isReadyToCraftTM(state, world, pos, tm!!)) || isReadyToCraftBlankTM(state, world, pos)) {
                // Get the direction the block is facing
                val facingDirection = state.get(Properties.FACING)?.opposite ?: return

                // Calculate the center position of the block
                val frontOffset = 0.5 // Half block offset to the front
                val spawnX = pos.x + 0.5 + facingDirection.offsetX * frontOffset
                val spawnY = pos.y + 0.3 + facingDirection.offsetY * frontOffset
                val spawnZ = pos.z + 0.5 + facingDirection.offsetZ * frontOffset

                // Create the ItemEntity at the center of the block
                val itemEntity = ItemEntity(world, spawnX, spawnY, spawnZ, itemStack).copy()

                // Create the ItemEntity
                itemEntity.setVelocity(0.0, 0.0, 0.0)

                // Add the ItemEntity to the world
                world.spawnEntity(itemEntity)

                //clear inventory of SidedInventory
                inventory.items?.clear()
                //getInventory(state, world, pos).clear()
                }
            }*/
    }override fun getRenderType(blockState: BlockState): BlockRenderType {
        return BlockRenderType.MODEL
    }

    override fun getFluidState(state: BlockState): FluidState {
        return if (state[WATERLOGGED]) {
            Fluids.WATER.getStill(false)
        } else super.getFluidState(state)
    }

    override fun getOutlineShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        context: ShapeContext
    ): VoxelShape {
        return when (state[FACING]) {
            Direction.NORTH -> NORTH_OUTLINE
            Direction.SOUTH -> SOUTH_OUTLINE
            Direction.WEST -> WEST_OUTLINE
            else -> EAST_OUTLINE
        }
    }
}
