/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.block.entity.BerryBlockEntity
import com.cobblemon.mod.common.item.berry.BerryItem
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
 * @author Apion
 * @since February 27, 2024
 */
class BindingSoilBlock(settings: Settings) : Block(settings) {
    @Deprecated("Deprecated in Java")
    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult
    ): ActionResult {
        if (!world.isClient) {
            val itemStack = player.getStackInHand(hand)
            val item = itemStack.item
            if (item is BerryItem) {
                world.setBlockState(pos, item.block.defaultState.with(BerryBlock.IS_ROOTED, true))
                if (!player.isCreative) {
                    itemStack.decrement(1)
                }
            }
        }
        return ActionResult.SUCCESS
    }
}