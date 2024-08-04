/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.brewing.ingredient

import net.minecraft.world.item.ItemStack
import org.jetbrains.annotations.ApiStatus

/**
 * The Cobblemon representation of a brewing ingredient.
 * This is done to abstract platform specific requirements for a more custom solution to brewing.
 */
@ApiStatus.Internal
sealed interface CobblemonIngredient {

    /**
     * Tests if a given [stack] is valid for this ingredient.
     *
     * @param stack The [ItemStack] being tested.
     * @return If the given [stack] is valid for this ingredient.
     */
    fun matches(stack: ItemStack): Boolean

    /**
     * Resolves list of [ItemStack]s that is used by the vanilla Ingredient implementation when a platform requires wrapping.
     *
     * @return A list of [ItemStack]s
     */
    fun matchingStacks(): List<ItemStack>

}