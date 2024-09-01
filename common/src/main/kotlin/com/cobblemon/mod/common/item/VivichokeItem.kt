/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.block.VivichokeBlock
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemNameBlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.PlaceOnWaterBlockItem
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level

class VivichokeItem(block: VivichokeBlock) : ItemNameBlockItem(block, Properties()) {
    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val blockHitResult = PlaceOnWaterBlockItem.getPlayerPOVHitResult(world, user, ClipContext.Fluid.SOURCE_ONLY)
        val blockHitResult2 = blockHitResult.withPosition(blockHitResult.blockPos.above())
        val actionResult: InteractionResult = super.useOn(UseOnContext(user, hand, blockHitResult2))
        return InteractionResultHolder(actionResult, user.getItemInHand(hand))
    }
}
