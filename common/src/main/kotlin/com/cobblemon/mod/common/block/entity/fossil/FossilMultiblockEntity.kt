/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.entity.fossil

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.api.multiblock.MultiblockEntity
import com.cobblemon.mod.common.block.multiblock.FossilMultiblockStructure
import com.cobblemon.mod.common.api.multiblock.MultiblockStructure
import com.cobblemon.mod.common.api.multiblock.builder.MultiblockStructureBuilder
import com.cobblemon.mod.common.util.DataKeys
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import net.minecraft.util.math.BlockPos

open class FossilMultiblockEntity(
    pos: BlockPos,
    state: BlockState,
    multiblockBuilder: MultiblockStructureBuilder,
    type: BlockEntityType<*> = CobblemonBlockEntities.FOSSIL_MULTIBLOCK
) : MultiblockEntity(type, pos, state, multiblockBuilder) {

    override var masterBlockPos: BlockPos? = null

    override var multiblockStructure: MultiblockStructure? = null
        set(structure) {
            field = structure
            if (structure != null) {
                masterBlockPos = structure.controllerBlockPos
            }
        }
        get() {
            return if (field != null) {
                field
            } else if (masterBlockPos != null && masterBlockPos != pos && world?.getBlockEntity(masterBlockPos) != null) {
                field = (world?.getBlockEntity(masterBlockPos) as FossilMultiblockEntity).multiblockStructure
                field
            } else {
                null
            }
        }

    override fun readNbt(nbt: NbtCompound) {
        multiblockStructure = if (nbt.contains(DataKeys.MULTIBLOCK_STORAGE)) {
            FossilMultiblockStructure.fromNbt(nbt.getCompound(DataKeys.MULTIBLOCK_STORAGE))
        } else {
            null
        }
        masterBlockPos = if (nbt.contains(DataKeys.CONTROLLER_BLOCK)) {
            NbtHelper.toBlockPos(nbt.getCompound(DataKeys.CONTROLLER_BLOCK))
        } else {
            null
        }
    }

}