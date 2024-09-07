/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.fossil

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.pokemon.evolution.predicate.NbtItemPredicate
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.StringRepresentable
import net.minecraft.world.item.ItemStack

class Fossil(
    identifier: ResourceLocation,
    val result: PokemonProperties,
    val fossils: List<NbtItemPredicate>
): StringRepresentable {

    @Transient
    var identifier: ResourceLocation = identifier
        internal set

    override fun getSerializedName(): String {
        return identifier.toString()
    }

    /**
     * Gets the name of the fossil.
     * @return The name of the fossil.
     */
    fun getName(): MutableComponent = Component.translatable("${identifier.namespace}.fossil.${identifier.path}.name")

    /**
     * Whether the fossil ingredients match this fossil.
     * @param ingredients The ingredients to check.
     * @return True if the ingredients match, false otherwise.
     */
    fun matchesIngredients(ingredients: List<ItemStack>): Boolean {
        if (this.fossils.size != ingredients.size) {
            return false
        }

        return this.fossils.all { fossil ->
            ingredients.any { fossil.test(it) }
        }
    }

    /**
     * Whether the fossil ingredients match a subset of this fossil.
     * @param ingredients The ingredients to check.
     * @return True if the ingredients are a subset, false otherwise.
     */
    fun matchesIngredientsSubSet(ingredients: List<ItemStack>): Boolean {
        if (this.fossils.size < ingredients.size) {
            return false
        }

        return ingredients.all { ingredient ->
            ingredients.count { item -> ingredient.`is`(item.itemHolder) }  <=
                    this.fossils.count { fossil -> fossil.test(ingredient) }
        }
    }

    /**
     * Whether the [ItemStack] is an ingredient for this fossil.
     * @param itemStack The [ItemStack] to check.
     * @return True if the [ItemStack] is an ingredient, false otherwise.
     */
    fun isIngredient(itemStack: ItemStack): Boolean {
        return this.fossils.any { it.test(itemStack) }
    }

}