/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.entity

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.block.chest.GildedChestBlock.Type
import com.cobblemon.mod.common.block.chest.GildedState
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.world.ContainerHelper
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.entity.ContainerOpenersCounter
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties

class GildedChestBlockEntity(pos: BlockPos, state: BlockState, val type: Type = Type.RED) : RandomizableContainerBlockEntity(CobblemonBlockEntities.GILDED_CHEST, pos, state), WorldlyContainer {
    var inventoryContents: NonNullList<ItemStack> = NonNullList.withSize(NUM_SLOTS, ItemStack.EMPTY)
    val posableState: GildedState = GildedState()

    private val stateManager: ContainerOpenersCounter = object : ContainerOpenersCounter() {
        override fun onOpen(world: Level, pos: BlockPos, state: BlockState) {
            playSound(world, pos, state, CobblemonSounds.GILDED_CHEST_OPEN)
        }

        override fun onClose(world: Level, pos: BlockPos, state: BlockState) {
            playSound(world, pos, state, CobblemonSounds.GILDED_CHEST_CLOSE)
        }

        override fun openerCountChanged(
            world: Level,
            pos: BlockPos,
            state: BlockState,
            oldViewerCount: Int,
            newViewerCount: Int
        ) {
            this@GildedChestBlockEntity.onViewerCountUpdate(world, pos, state, oldViewerCount, newViewerCount)
        }

        override fun isOwnContainer(player: Player): Boolean {
            if (player.containerMenu is ChestMenu) {
                val inventory = (player.containerMenu as ChestMenu).container
                return inventory === this@GildedChestBlockEntity
            }
            return false
        }
    }

    override fun getType(): BlockEntityType<*> = CobblemonBlockEntities.GILDED_CHEST

    override fun getContainerSize() = NUM_SLOTS

    override fun getDefaultName() = Component.translatable("block.cobblemon.gilded_chest")

    override fun createMenu(syncId: Int, playerInventory: Inventory): AbstractContainerMenu =
        ChestMenu.threeRows(syncId, playerInventory, this)

    override fun getItems() = inventoryContents

    override fun startOpen(player: Player) {
        if (!this.remove && !player.isSpectator && type != Type.FAKE) {
            stateManager.incrementOpeners(player, level, blockPos, blockState)
        }
    }

    override fun stopOpen(player: Player) {
        if (!this.remove && !player.isSpectator) {
            stateManager.decrementOpeners(player, level, blockPos, blockState)
        }
    }

    override fun getSlotsForFace(side: Direction): IntArray {
        return if (type == Type.FAKE) IntArray(0) else IntArray(NUM_SLOTS) { it }
    }

    override fun canPlaceItemThroughFace(slot: Int, stack: ItemStack, dir: Direction?): Boolean {
        if (type == Type.FAKE) return false
        return dir != Direction.DOWN
    }

    override fun canTakeItemThroughFace(slot: Int, stack: ItemStack, dir: Direction): Boolean {
        if (type == Type.FAKE) return false
        return dir == Direction.DOWN
    }

    override fun stillValid(player: Player) = !player.isSpectator

    override fun setItems(inventory: NonNullList<ItemStack>) {
        inventoryContents = inventory
    }

    companion object {
        val NUM_SLOTS = 27
        fun playSound(world: Level, pos: BlockPos, state: BlockState, sound: SoundEvent) {
            var d = pos.x.toDouble() + 0.5
            val e = pos.y.toDouble() + 0.5
            var f = pos.z.toDouble() + 0.5
            val direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING)
            d += direction.stepX.toDouble() * 0.5
            f += direction.stepZ.toDouble() * 0.5
            world.playSound(
                null,
                d,
                e,
                f,
                sound,
                SoundSource.BLOCKS,
                0.5f,
                world.random.nextFloat() * 0.1f + 0.9f
            )
        }
    }

    override fun triggerEvent(type: Int, data: Int): Boolean {
        if (type == 1) {
            val isNowOpen = data > 0
            val wasOpen = posableState.currentPose == "open"
            val model = posableState.currentModel ?: return true
            if (isNowOpen && !wasOpen) {
                model.moveToPose(posableState, model.poses["open"]!!)
            } else if (!isNowOpen && wasOpen) {
                model.moveToPose(posableState, model.poses["closed"]!!)
            }
            return true
        }
        return super.triggerEvent(type, data)
    }

    fun onViewerCountUpdate(world: Level, pos: BlockPos, state: BlockState, oldViewerCount: Int, newViewerCount: Int) {
        val block = state.block
        world.blockEvent(pos, block, 1, newViewerCount)
    }

    override fun saveAdditional(nbt: CompoundTag, registryLookup: HolderLookup.Provider) {
        super.saveAdditional(nbt, registryLookup)
        if (!trySaveLootTable(nbt)) {
            ContainerHelper.saveAllItems(nbt, inventoryContents, registryLookup)
        }
    }

    override fun loadAdditional(nbt: CompoundTag, registryLookup: HolderLookup.Provider) {
        super.loadAdditional(nbt, registryLookup)
        inventoryContents= NonNullList.withSize(containerSize, ItemStack.EMPTY)
        if (!tryLoadLootTable(nbt)) {
            ContainerHelper.loadAllItems(nbt, inventoryContents, registryLookup)
        }
    }

    fun tick() {
        if (!this.remove) {
            stateManager.recheckOpeners(level, blockPos, this.blockState)
        }
    }
}