/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.battle

import net.minecraft.world.item.ItemStack

/**
 * A type of [BagItemLike] that just checks the item type of the stack.
 *
 * Used for internal Item subclasses.
 *
 * @author Hiroku
 * @since July 1st, 2023
 */
interface SimpleBagItemLike : BagItemLike {
    val bagItem: BagItem
    override fun getBagItem(stack: ItemStack) = stack.item.takeIf { it == this }?.let { bagItem }
}