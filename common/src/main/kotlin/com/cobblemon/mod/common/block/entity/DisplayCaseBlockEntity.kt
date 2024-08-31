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
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.sounds.SoundSource
import net.minecraft.world.ContainerHelper
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class DisplayCaseBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(CobblemonBlockEntities.DISPLAY_CASE, pos, state),
    WorldlyContainer {

    val inv: NonNullList<ItemStack> = NonNullList.withSize(1, ItemStack.EMPTY)

    /**
     * Updates the [ItemStack] stored in this entity
     *
     * @param player The [Player] that performed this interaction
     * @param hand The [Hand] the player used to perform this interaction
     * @author whatsy
     */
    fun updateItem(player: Player, hand: InteractionHand): InteractionResult {
        val playerStack = player.getItemInHand(hand)

        // Player and case item are the same - do nothing
        if (playerStack.item == getStack().item) {
            return if (playerStack.item != Items.AIR) InteractionResult.SUCCESS else InteractionResult.FAIL
        }

        // Player's hand is empty, case is not empty - give player the item in the case
        if (playerStack.isEmpty && !getStack().isEmpty) {
            if (!player.isCreative) player.setItemInHand(hand, getStack())
            setCaseStack(ItemStack.EMPTY)
            return InteractionResult.sidedSuccess(true)
        }

        // Case is empty, player's hand is not - put playerStack in the case
        if (getStack().isEmpty && !playerStack.isEmpty) {
            setCaseStack(playerStack.copy())
            if (!player.isCreative) playerStack.shrink(1)
            return InteractionResult.sidedSuccess(true)
        }

        // Player has item, case has item - swap items
        if (!getStack().isEmpty && !playerStack.isEmpty) {
            val oldCaseStack = getStack()
            setCaseStack(playerStack.copy())
            if (!player.isCreative) {
                playerStack.shrink(1)
                player.addItem(oldCaseStack)
            }

            return InteractionResult.SUCCESS
        }

        return InteractionResult.FAIL
    }

    fun getStack(): ItemStack {
        return inv[0]
    }

    private fun setCaseStack(newStack: ItemStack) {
        val level = level ?: return
        val oldState = level.getBlockState(blockPos)
        newStack.count = 1
        inv[0] = newStack
        if (newStack.isEmpty) {
            level.playSound(null, blockPos, CobblemonSounds.DISPLAY_CASE_REMOVE_ITEM, SoundSource.BLOCKS)
        } else {
            level.playSound(null, blockPos, CobblemonSounds.DISPLAY_CASE_ADD_ITEM, SoundSource.BLOCKS)
        }
        onItemUpdated(level, oldState, level.getBlockState(blockPos))
    }

    override fun saveAdditional(nbt: CompoundTag, registryLookup: HolderLookup.Provider) {
        super.saveAdditional(nbt, registryLookup)
        ContainerHelper.saveAllItems(nbt, inv, true, registryLookup)
    }

    override fun loadAdditional(nbt: CompoundTag, registryLookup: HolderLookup.Provider) {
        super.loadAdditional(nbt, registryLookup)
        inv.clear()
        ContainerHelper.loadAllItems(nbt, inv, registryLookup)
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    override fun getUpdateTag(registryLookup: HolderLookup.Provider): CompoundTag {
        return this.saveWithoutMetadata(registryLookup)
    }

    private fun onItemUpdated(world: Level, oldState: BlockState, newState: BlockState) {
        world.sendBlockUpdated(blockPos, oldState, newState, Block.UPDATE_CLIENTS)
        world.updateNeighbourForOutputSignal(blockPos, world.getBlockState(blockPos).block)
        setChanged()
    }

    override fun clearContent() {
        inv.clear()
    }

    override fun getContainerSize(): Int {
        return inv.size
    }

    override fun isEmpty(): Boolean {
        return getStack().isEmpty
    }

    override fun getItem(slot: Int): ItemStack {
        return getStack()
    }

    override fun removeItem(slot: Int, amount: Int): ItemStack {
        val oldState = blockState
        val result = ContainerHelper.removeItem(inv, slot, amount)
        if (level != null) onItemUpdated(level!!, oldState, level!!.getBlockState(blockPos))
        return result
    }

    override fun removeItemNoUpdate(slot: Int): ItemStack {
        val oldState = blockState
        val result = ContainerHelper.takeItem(inv, slot)
        if (level != null) onItemUpdated(level!!, oldState, level!!.getBlockState(blockPos))
        return result
    }

    override fun setItem(slot: Int, stack: ItemStack) {
        val oldState = blockState
        inv[slot] = stack
        if (stack.count > stack.maxStackSize) {
            stack.count = stack.maxStackSize
        }
        if (level != null) onItemUpdated(level!!, oldState, level!!.getBlockState(blockPos))
    }

    override fun stillValid(player: Player): Boolean = false

    override fun getSlotsForFace(side: Direction): IntArray {
        val result = IntArray(inv.size)
        for (i in result.indices) {
            result[i] = i
        }
        return result
    }

    override fun getMaxStackSize() = 1

    override fun canPlaceItemThroughFace(slot: Int, stack: ItemStack, dir: Direction?): Boolean {
        if(dir == Direction.DOWN) return false
        return getStack().isEmpty
    }

    override fun canTakeItemThroughFace(slot: Int, stack: ItemStack, direction: Direction): Boolean = (direction == Direction.DOWN)
}