/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.block.MedicinalLeekBlock
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemNameBlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.PlaceOnWaterBlockItem
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level

class MedicinalLeekItem(block: MedicinalLeekBlock, settings: Properties) : ItemNameBlockItem(block, settings) {

    init {
        Cobblemon.implementation.registerCompostable(this, .65F)
    }

    override fun useOn(context: UseOnContext): InteractionResult = InteractionResult.PASS

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val blockHitResult = PlaceOnWaterBlockItem.getPlayerPOVHitResult(world, user, ClipContext.Fluid.SOURCE_ONLY)
        val blockHitResult2 = blockHitResult.withPosition(blockHitResult.blockPos.above())
        val placeResult = this.place(BlockPlaceContext(UseOnContext(user, hand, blockHitResult2)))
        val stack = user.getItemInHand(hand)
        // This will always be true but just in case we change it down the line.
        /*
        if (!placeResult.isAccepted && this.isFood && user.canConsume(this.foodComponent?.isAlwaysEdible == true)) {
            user.setCurrentHand(hand)
            return TypedInteractionResult.consume(stack)
        }

         */
        return InteractionResultHolder(placeResult, stack)
    }

}