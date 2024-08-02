/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.moves.categories

import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

object DamageCategories {
    private val allCategories = mutableListOf<DamageCategory>()

    val PHYSICAL = register(
        name = "physical",
        displayName = Component.translatable("cobblemon.move.category.physical"),
        textureXMultiplier = 0
    )
    val SPECIAL = register(
        name = "special",
        displayName = Component.translatable("cobblemon.move.category.special"),
        textureXMultiplier = 1
    )
    val STATUS = register(
        name = "status",
        displayName = Component.translatable("cobblemon.move.category.status"),
        textureXMultiplier = 2
    )

    fun register(
        name: String,
        displayName: Component,
        resourceLocation: ResourceLocation = cobblemonResource("textures/gui/categories.png"),
        textureXMultiplier: Int
    ): DamageCategory {
        return DamageCategory(
            name = name,
            displayName = displayName,
            textureXMultiplier = textureXMultiplier,
            resourceLocation = resourceLocation
        ).also {
            allCategories.add(it)
        }
    }

    fun register(damageCategory: DamageCategory): DamageCategory {
        allCategories.add(damageCategory)
        return damageCategory
    }

    fun get(name: String): DamageCategory? {
        return allCategories.firstOrNull { cat -> cat.name.equals(name, ignoreCase = true) }
    }

    fun getOrException(name: String): DamageCategory {
        return allCategories.first { cat -> cat.name.equals(name, ignoreCase = true) }
    }

    fun count() = allCategories.size
}