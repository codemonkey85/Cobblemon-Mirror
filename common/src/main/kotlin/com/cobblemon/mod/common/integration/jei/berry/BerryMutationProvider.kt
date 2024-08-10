/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.integration.jei.berry

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.integration.jei.CobblemonJeiProvider
import com.cobblemon.mod.common.item.berry.BerryItem
import mezz.jei.api.registration.IRecipeCategoryRegistration
import mezz.jei.api.registration.IRecipeRegistration

class BerryMutationProvider : CobblemonJeiProvider {
    override fun registerCategory(registration: IRecipeCategoryRegistration) {
        registration.addRecipeCategories(BerryRecipeCategory(registration))
    }

    override fun registerRecipes(registration: IRecipeRegistration) {
        val berryList = CobblemonItems.berries().map { it.value }
        val berryMutations = mutableSetOf<Triple<BerryItem, BerryItem, BerryItem>>()
        berryList.forEach {berry ->
            for (mut in berry.berry()?.mutations?.entries ?: emptySet()) {
                val berryTwo = CobblemonItems.berries()[mut.key] ?: continue
                val berryThree = CobblemonItems.berries()[mut.value] ?: continue
                val mutation = Triple(berry, berryTwo, berryThree)
                val altMutation = Triple(berryTwo, berry, berryThree)
                if (!(berryMutations.contains(mutation) || berryMutations.contains(altMutation))) {
                    berryMutations.add(mutation)
                }
            }
        }
        val berryMutationRecipes = berryMutations.map { BerryMutationRecipe(it.first, it.second, it.third) }
        registration.addRecipes(BerryRecipeCategory.RECIPE_TYPE, berryMutationRecipes)
    }


}
