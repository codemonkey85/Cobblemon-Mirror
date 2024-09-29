/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.pokedex.PokedexTypes
import net.minecraft.client.player.LocalPlayer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.UseAnim
import net.minecraft.world.level.Level

class PokedexItem(val type: PokedexTypes): CobblemonItem(Item.Properties().stacksTo(1)) {

    override fun getUseAnimation(itemStack: ItemStack): UseAnim? = UseAnim.TOOT_HORN

    override fun getUseDuration(stack: ItemStack, user: LivingEntity): Int = 72000

    override fun use(
        world: Level,
        player: Player,
        usedHand: InteractionHand
    ): InteractionResultHolder<ItemStack> {
        val itemStack = player.getItemInHand(usedHand)
        if (world.isClientSide && player is LocalPlayer) {
            CobblemonClient.pokedexUsageContext.type = type
        }
        if (player !is ServerPlayer) return InteractionResultHolder.consume(itemStack)
        //Disables breaking blocks and damaging entities
        player.startUsingItem(usedHand)
        return InteractionResultHolder.fail(itemStack)
    }

    override fun onUseTick(
        world: Level,
        user: LivingEntity,
        stack: ItemStack,
        remainingUseTicks: Int
    ) {
        if (world.isClientSide && user is LocalPlayer) {
            val usageContext = CobblemonClient.pokedexUsageContext
            val ticksInUse = getUseDuration(stack, user) - remainingUseTicks
            usageContext.useTick(user, ticksInUse, true)
        }
        super.onUseTick(world, user, stack, remainingUseTicks)
    }

    override fun releaseUsing(
        stack: ItemStack,
        world: Level,
        user: LivingEntity,
        remainingUseTicks: Int
    ) {
        if (world.isClientSide && user is LocalPlayer) {
            val usageContext = CobblemonClient.pokedexUsageContext
            val ticksInUse = getUseDuration(stack, user) - remainingUseTicks
            usageContext.stopUsing(user, ticksInUse)
        }

        super.releaseUsing(stack, world, user, remainingUseTicks)
    }
}
