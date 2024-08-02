/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.fabric.brewing

import com.cobblemon.mod.common.brewing.BrewingRecipes
import net.minecraft.world.item.ItemStack

object CobblemonFabricBreweryRegistry {

    fun isValidPotionSlot(stack: ItemStack): Boolean = BrewingRecipes.recipes.any { it.first.matches(stack) }

    fun isValidIngredientSlot(stack: ItemStack): Boolean = BrewingRecipes.recipes.any { it.second.matches(stack) }

    fun hasRecipe(input: ItemStack, ingredient: ItemStack): Boolean = BrewingRecipes.recipes.any { it.first.matches(input) && it.second.matches(ingredient) }

    fun recipeResultOf(input: ItemStack, ingredient: ItemStack): ItemStack = BrewingRecipes.recipes.firstOrNull { it.first.matches(input) && it.second.matches(ingredient) }?.third?.defaultInstance ?: ItemStack.EMPTY


}