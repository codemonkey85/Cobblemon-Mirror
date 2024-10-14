/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.entity

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.block.sign.CobblemonHangingSignBlock
import com.cobblemon.mod.common.block.sign.CobblemonWallHangingSignBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.entity.HangingSignBlockEntity
import net.minecraft.core.BlockPos

class CobblemonHangingSignBlockEntity(pos: BlockPos, state: BlockState) : HangingSignBlockEntity(pos, state) {

    override fun getType(): BlockEntityType<*> = CobblemonBlockEntities.HANGING_SIGN

    override fun isValidBlockState(blockState: BlockState): Boolean {
        return blockState.block is CobblemonHangingSignBlock || blockState.block is CobblemonWallHangingSignBlock
    }
}