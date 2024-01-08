/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.entity

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.block.chest.GildedState
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.entity.LootableContainerBlockEntity
import net.minecraft.block.entity.ViewerCountManager
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.DoubleInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.state.property.Properties
import net.minecraft.text.Text
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class GildedChestBlockEntity(pos: BlockPos, state: BlockState) : LootableContainerBlockEntity(CobblemonBlockEntities.GILDED_CHEST, pos, state) {
    var inventoryContents: DefaultedList<ItemStack> = DefaultedList.ofSize(NUM_SLOTS, ItemStack.EMPTY)
    val poseableState: GildedState = GildedState()

    private val stateManager: ViewerCountManager = object : ViewerCountManager() {
        override fun onContainerOpen(world: World, pos: BlockPos, state: BlockState) {
            playSound(world, pos, state, SoundEvents.BLOCK_CHEST_OPEN)
        }

        override fun onContainerClose(world: World, pos: BlockPos, state: BlockState) {
            playSound(world, pos, state, SoundEvents.BLOCK_CHEST_CLOSE)
        }

        override fun onViewerCountUpdate(
            world: World,
            pos: BlockPos,
            state: BlockState,
            oldViewerCount: Int,
            newViewerCount: Int
        ) {
            this@GildedChestBlockEntity.onViewerCountUpdate(world, pos, state, oldViewerCount, newViewerCount)
        }

        override fun isPlayerViewing(player: PlayerEntity): Boolean {
            if (player.currentScreenHandler is GenericContainerScreenHandler) {
                val inventory = (player.currentScreenHandler as GenericContainerScreenHandler).inventory
                return inventory === this@GildedChestBlockEntity || inventory is DoubleInventory && inventory.isPart(
                    this@GildedChestBlockEntity
                )
            }
            return false
        }
    }

    override fun getType(): BlockEntityType<*> = CobblemonBlockEntities.GILDED_CHEST
    override fun size() = NUM_SLOTS

    override fun getContainerName() = Text.of("Gilded Chest")
    override fun createScreenHandler(syncId: Int, playerInventory: PlayerInventory?) =
        GenericContainerScreenHandler.createGeneric9x3(syncId, playerInventory, this);

    override fun onOpen(player: PlayerEntity) {
        if (!this.removed && !player.isSpectator) {
            stateManager.openContainer(player, this.getWorld(), this.getPos(), this.cachedState)
        }
    }

    override fun onClose(player: PlayerEntity) {
        if (!this.removed && !player.isSpectator) {
            stateManager.closeContainer(player, this.getWorld(), this.getPos(), this.cachedState)
        }
    }

    override fun getInvStackList(): DefaultedList<ItemStack> = inventoryContents

    override fun setInvStackList(list: DefaultedList<ItemStack>) {
        inventoryContents = list
    }

    companion object {
        val NUM_SLOTS = 27
        fun playSound(world: World, pos: BlockPos, state: BlockState, sound: net.minecraft.sound.SoundEvent) {
            var d = pos.x.toDouble() + 0.5
            val e = pos.y.toDouble() + 0.5
            var f = pos.z.toDouble() + 0.5
            val direction = state.get(Properties.HORIZONTAL_FACING)
            d += direction.offsetX.toDouble() * 0.5
            f += direction.offsetZ.toDouble() * 0.5
            world.playSound(
                null,
                d,
                e,
                f,
                sound,
                SoundCategory.BLOCKS,
                0.5f,
                world.random.nextFloat() * 0.1f + 0.9f
            )
        }
    }

    override fun onSyncedBlockEvent(type: Int, data: Int): Boolean {
        if (type == 1) {
            val isNowOpen = data > 0
            val wasOpen = poseableState.currentPose == "OPEN"
            val model = poseableState.currentModel ?: return true
            if (isNowOpen && !wasOpen) {
                model.moveToPose(null, poseableState, model.getPose("OPEN")!!)
            } else if (!isNowOpen && wasOpen) {
                model.moveToPose(null, poseableState, model.getPose("CLOSED")!!)
            }
            return true
        }
        return super.onSyncedBlockEvent(type, data)
    }

    fun onViewerCountUpdate(world: World, pos: BlockPos, state: BlockState, oldViewerCount: Int, newViewerCount: Int) {
        val block = state.block
        world.addSyncedBlockEvent(pos, block, 1, newViewerCount)
    }
}