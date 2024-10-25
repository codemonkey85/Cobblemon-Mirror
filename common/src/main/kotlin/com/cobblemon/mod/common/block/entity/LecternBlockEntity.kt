/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.entity

import com.cobblemon.mod.common.CobblemonBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.ContainerHelper
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class LecternBlockEntity(blockPos: BlockPos, blockState: BlockState) : BlockEntity(CobblemonBlockEntities.LECTERN, blockPos, blockState) {
    val inventory: NonNullList<ItemStack> = NonNullList.withSize(1, ItemStack.EMPTY)

    override fun saveAdditional(compoundTag: CompoundTag, registryLookup: HolderLookup.Provider) {
        super.saveAdditional(compoundTag, registryLookup)
        ContainerHelper.saveAllItems(compoundTag, inventory, true, registryLookup)
    }

    override fun loadAdditional(compoundTag: CompoundTag, registryLookup: HolderLookup.Provider) {
        super.loadAdditional(compoundTag, registryLookup)
        ContainerHelper.loadAllItems(compoundTag, inventory, registryLookup)
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    override fun getUpdateTag(registryLookup: HolderLookup.Provider): CompoundTag {
        return this.saveWithoutMetadata(registryLookup)
    }

    fun isEmpty(): Boolean = getItemStack().isEmpty

    fun getItemStack(): ItemStack = inventory[0]

    fun setItemStack(itemStack: ItemStack) {
        if (level != null) {
            inventory[0] = itemStack
            onItemUpdate(level!!)
        }
    }

    fun removeItemStack(): ItemStack {
        if (level != null) {
            val itemStack = ContainerHelper.removeItem(inventory, 0, 1)
            onItemUpdate(level!!)
            return itemStack
        }
        return ItemStack.EMPTY
    }

    private fun onItemUpdate(level: Level) {
        val oldState = level.getBlockState(blockPos)
        level.sendBlockUpdated(blockPos, oldState, level.getBlockState(blockPos), Block.UPDATE_ALL)
        level.updateNeighbourForOutputSignal(blockPos, level.getBlockState(blockPos).block)
        setChanged()
    }
}