/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.predicate

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import net.minecraft.advancements.critereon.NbtPredicate
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

data class NbtItemPredicate(
    val item: RegistryLikeCondition<Item>,
    val nbt: NbtPredicate? = null
) {
    fun test(item: ItemStack): Boolean {
        return this.item.fits(item.item, BuiltInRegistries.ITEM) && (this.nbt?.matches(item) ?: true)
    }
}