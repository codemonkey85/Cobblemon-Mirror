/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.tooltips

import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

abstract class TooltipGenerator {
    open fun generateTooltip(stack: ItemStack, lines: MutableList<Component>): MutableList<Component>? {
        return null
    }
    open fun generateAdditionalTooltip(stack: ItemStack, lines: MutableList<Component>): MutableList<Component>? {
        return null
    }
    open fun generateCategoryTooltip(stack: ItemStack, lines: MutableList<Component>): MutableList<Component>? {
        return null
    }
}