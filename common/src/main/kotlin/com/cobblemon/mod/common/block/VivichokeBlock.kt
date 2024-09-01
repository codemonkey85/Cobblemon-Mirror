/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.CobblemonItems
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.CropBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

@Suppress("OVERRIDE_DEPRECATION")
class VivichokeBlock(settings: Properties) : CropBlock(settings) {

    override fun getShape(
        state: BlockState,
        blockGetter: BlockGetter,
        pos: BlockPos,
        collisionContext: CollisionContext
    ): VoxelShape = AGE_TO_SHAPE.getOrElse(state.getValue(this.ageProperty)) { Shapes.block() }

    // This is a design choice, they shouldn't grow more than a single stage at a time.
    override fun getBonemealAgeIncrease(world: Level): Int = 1

    override fun getBaseSeedId(): ItemLike = CobblemonItems.VIVICHOKE_SEEDS

    override fun codec(): MapCodec<out CropBlock> {
        return CODEC
    }

    companion object {
        val CODEC = simpleCodec(::VivichokeBlock)

        private val STAGE_0_SHAPE = box(6.0, -1.0, 6.0, 10.0, 2.0, 10.0)
        private val STAGE_1_SHAPE = box(6.0, -1.0, 6.0, 10.0, 5.0, 10.0)
        private val STAGE_2_SHAPE = box(6.0, -1.0, 6.0, 10.0, 7.0, 10.0)
        private val STAGE_3_SHAPE = box(6.0, -1.0, 6.0, 10.0, 9.0, 10.0)
        private val STAGE_4_SHAPE = box(6.0, -1.0, 6.0, 10.0, 7.0, 10.0)
        private val STAGE_5_SHAPE = box(6.0, -1.0, 6.0, 10.0, 8.0, 10.0)
        private val STAGE_6_SHAPE = box(6.0, -1.0, 6.0, 10.0, 9.0, 10.0)
        private val STAGE_7_SHAPE = Shapes.or(
            box(6.0, -1.0, 6.0, 10.0, 10.0, 10.0),
            box(5.5, 10.0, 5.5, 10.5, 14.0, 10.5)
        )

        /**
         * An array with the equivalent [VoxelShape] of a growth stage as the index.
         */
        val AGE_TO_SHAPE = arrayOf(
            STAGE_0_SHAPE,
            STAGE_1_SHAPE,
            STAGE_2_SHAPE,
            STAGE_3_SHAPE,
            STAGE_4_SHAPE,
            STAGE_5_SHAPE,
            STAGE_6_SHAPE,
            STAGE_7_SHAPE
        )

    }
}