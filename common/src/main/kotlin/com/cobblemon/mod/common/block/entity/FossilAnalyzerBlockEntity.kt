/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.entity

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.api.fossil.Fossils
import com.cobblemon.mod.common.api.multiblock.builder.MultiblockStructureBuilder
import com.cobblemon.mod.common.block.multiblock.FossilMultiblockStructure
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState

class FossilAnalyzerBlockEntity(
    pos: BlockPos, state: BlockState,
    multiblockBuilder: MultiblockStructureBuilder
): FossilMultiblockEntity(pos, state, multiblockBuilder, CobblemonBlockEntities.FOSSIL_ANALYZER) {

    val inv = FossilAnalyzerInventory(this)

    class FossilAnalyzerInventory(val analyzerEntity: FossilAnalyzerBlockEntity) : WorldlyContainer {
        override fun clearContent() {
            if(analyzerEntity.multiblockStructure != null && analyzerEntity.multiblockStructure is FossilMultiblockStructure) {
                val fossilMultiblockStructure = analyzerEntity.multiblockStructure as FossilMultiblockStructure
                fossilMultiblockStructure.fossilInventory.clear()
            }
        }

        override fun getContainerSize(): Int {
            if (analyzerEntity.multiblockStructure != null) {
                return 3
            }
            return 0
        }

        override fun isEmpty(): Boolean {
            return false
        }

        override fun getItem(slot: Int): ItemStack {
            if (analyzerEntity.multiblockStructure != null && analyzerEntity.multiblockStructure is FossilMultiblockStructure) {
                val fossilMultiblockStructure = analyzerEntity.multiblockStructure as FossilMultiblockStructure
                if (fossilMultiblockStructure.fossilInventory.size > slot) {
                    return fossilMultiblockStructure.fossilInventory[slot]
                }
            }
            return ItemStack.EMPTY
        }

        //Unimplemented since no extract
        override fun removeItem(slot: Int, amount: Int): ItemStack {
            return removeItemNoUpdate(slot)
        }

        override fun removeItemNoUpdate(slot: Int): ItemStack {
            return ItemStack.EMPTY
        }

        override fun setItem(slot: Int, stack: ItemStack) {
            if (analyzerEntity.multiblockStructure != null && analyzerEntity.multiblockStructure is FossilMultiblockStructure) {
                val struct = analyzerEntity.multiblockStructure as FossilMultiblockStructure
                analyzerEntity.level?.let {
                    struct.insertFossil(stack, it)
                }
            }
        }

        override fun setChanged() {
            if (analyzerEntity.level != null) {
                analyzerEntity.multiblockStructure?.markDirty(analyzerEntity.level!!)
            }
        }

        override fun stillValid(player: Player) = false
        override fun getSlotsForFace(side: Direction): IntArray {
            if(analyzerEntity.multiblockStructure != null && analyzerEntity.multiblockStructure is FossilMultiblockStructure) {
                return intArrayOf(0,1,2)
            }
            return IntArray(0)
        }

        override fun canPlaceItemThroughFace(slot: Int, stack: ItemStack, dir: Direction?): Boolean {
            if (analyzerEntity.multiblockStructure is FossilMultiblockStructure) {
                val structure = analyzerEntity.multiblockStructure as FossilMultiblockStructure
                return stack?.let { Fossils.isFossilIngredient(it) } == true
                        && structure.fossilInventory.size < Cobblemon.config.maxInsertedFossilItems
                        && structure.isRunning() == false && structure.resultingFossil == null
                        && Fossils.getSubFossilByItemStacks( structure.fossilInventory + mutableListOf(stack) ) != null
            }
            return false
        }

        override fun canTakeItemThroughFace(slot: Int, stack: ItemStack, direction: Direction) = false
    }
}