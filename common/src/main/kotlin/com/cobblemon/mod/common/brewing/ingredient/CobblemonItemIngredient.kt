/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.brewing.ingredient

import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
class CobblemonItemIngredient(val item: Item) : CobblemonIngredient {

    override fun matches(stack: ItemStack): Boolean = stack.`is`(this.item)

    override fun matchingStacks(): List<ItemStack> = listOf(this.item.defaultInstance)

}