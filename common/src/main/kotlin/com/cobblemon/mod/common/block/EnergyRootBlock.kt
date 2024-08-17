/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.CobblemonItems
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

@Suppress("OVERRIDE_DEPRECATION")
class EnergyRootBlock(settings: Properties) : RootBlock(settings) {

    override fun getShape(state: BlockState, world: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = AABB

    override fun shearedResultingState(): BlockState = CobblemonBlocks.BIG_ROOT.defaultBlockState()

    override fun shearedDrop(): ItemStack = CobblemonItems.ENERGY_ROOT.defaultInstance

    override fun codec(): MapCodec<out Block> {
        return CODEC
    }

    companion object {
        private val CODEC = simpleCodec(::EnergyRootBlock)

        private val AABB = Shapes.box(0.2, 0.1, 0.2, 0.8, 1.0, 0.8)
    }

}