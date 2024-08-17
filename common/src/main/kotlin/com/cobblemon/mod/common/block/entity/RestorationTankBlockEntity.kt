/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.entity

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.api.fossil.NaturalMaterials
import com.cobblemon.mod.common.api.multiblock.builder.MultiblockStructureBuilder
import com.cobblemon.mod.common.block.multiblock.FossilMultiblockStructure
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.ContainerHelper
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState

class RestorationTankBlockEntity(
    pos: BlockPos, state: BlockState,
    multiblockBuilder: MultiblockStructureBuilder
) : FossilMultiblockEntity(pos, state, multiblockBuilder, CobblemonBlockEntities.RESTORATION_TANK) {
    val inv = RestorationTankInventory(this)

    override fun saveAdditional(nbt: CompoundTag, registryLookup: HolderLookup.Provider) {
        super.saveAdditional(nbt, registryLookup)
        ContainerHelper.saveAllItems(nbt, inv.items, registryLookup)
    }

    override fun loadAdditional(nbt: CompoundTag, registryLookup: HolderLookup.Provider) {
        super.loadAdditional(nbt, registryLookup)
        ContainerHelper.loadAllItems(nbt, inv.items, registryLookup)
    }

    class RestorationTankInventory(val tankEntity: RestorationTankBlockEntity) : WorldlyContainer {

        val items: NonNullList<ItemStack> = NonNullList.withSize(8, ItemStack.EMPTY)

        override fun clearContent() {
            for (i in items.indices) {
                items[i] = ItemStack.EMPTY
            }
        }

        override fun getContainerSize(): Int {
            return 8
        }

        override fun isEmpty(): Boolean {
            return items.all { it == ItemStack.EMPTY }
        }

        override fun getItem(slot: Int): ItemStack {
            if (slot == -1 || slot >= containerSize) {
                return ItemStack.EMPTY
            }
            return items[slot]
        }

        override fun removeItem(slot: Int, amount: Int): ItemStack {
            if (slot < 0 || slot >= containerSize) {
                return ItemStack.EMPTY
            }
            val result = items[slot].split(amount)
            if (items[slot].count == 0) {
                items[slot] = ItemStack.EMPTY
            }
            return result
        }

        override fun removeItemNoUpdate(slot: Int): ItemStack {
            return removeItem(slot, 1)
        }

        override fun setItem(slot: Int, stack: ItemStack) {
            val struct = tankEntity.multiblockStructure as? FossilMultiblockStructure
            tankEntity.level?.let {
                struct?.insertOrganicMaterial(stack, it)
                val returnIdentifier = NaturalMaterials.getReturnItem((stack))
                if (returnIdentifier != null) {
                    // Store the return item
                    val returnItem = BuiltInRegistries.ITEM.get(returnIdentifier)
                    storeReturnItem(returnItem, stack.count)
                }
            }
        }

        private fun storeReturnItem(returnItem: Item, count: Int) {
            val destStack = items.withIndex().firstOrNull {
                it.value == ItemStack.EMPTY || (it.value.count < it.value.maxStackSize && it.value.item == returnItem)
            }
            if (destStack != null) {
                if (destStack.value == ItemStack.EMPTY) {
                    items[destStack.index] = ItemStack(returnItem, 1)
                } else {
                    destStack.value.grow(count)
                }
            }
        }

        override fun setChanged() {
            tankEntity.level?.let {
                tankEntity.multiblockStructure?.markDirty(it)
            }
        }

        override fun stillValid(player: Player): Boolean {
            return false
        }

        override fun getSlotsForFace(side: Direction): IntArray {
            return if (side == Direction.DOWN) intArrayOf(0, 1, 2, 3, 4, 5, 6, 7) else intArrayOf(-1)
        }

        override fun canPlaceItemThroughFace(slot: Int, stack: ItemStack, direction: Direction?): Boolean {
            if (tankEntity.multiblockStructure is FossilMultiblockStructure) {
                if (direction != Direction.DOWN) {
                    val structure = tankEntity.multiblockStructure as FossilMultiblockStructure
                    val canUtilize = stack?.let { NaturalMaterials.isNaturalMaterial(it) } == true
                            && structure.organicMaterialInside < FossilMultiblockStructure.MATERIAL_TO_START
                            && structure.hasCreatedPokemon
                    val returnItem = NaturalMaterials.getReturnItem(stack!!) ?: return canUtilize
                    if (canUtilize) {
                        // See if there's room
                        for (i in items.indices) {
                            if (items[i] == ItemStack.EMPTY || (items[i].count < items[i].maxStackSize
                                        && items[i].item == BuiltInRegistries.ITEM.get(returnItem))
                            ) {
                                return true
                            }
                        }
                        return false
                    }
                }
            }
            return false
        }

        override fun canTakeItemThroughFace(slot: Int, stack: ItemStack, dir: Direction): Boolean {
            return dir == Direction.DOWN && slot >= 0 && slot < containerSize
        }

    }

}