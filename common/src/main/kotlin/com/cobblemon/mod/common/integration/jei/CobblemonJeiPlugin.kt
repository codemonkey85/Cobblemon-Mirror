/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.integration.jei

import com.cobblemon.mod.common.integration.jei.berry.BerryMutationProvider
import com.cobblemon.mod.common.integration.jei.berry.BerryRecipeCategory
import com.cobblemon.mod.common.util.cobblemonResource
import mezz.jei.api.IModPlugin
import mezz.jei.api.JeiPlugin
import mezz.jei.api.registration.IRecipeCategoryRegistration
import mezz.jei.api.registration.IRecipeRegistration
import net.minecraft.resources.ResourceLocation

@JeiPlugin
class CobblemonJeiPlugin : IModPlugin {
    private val jeiProviders: Set<CobblemonJeiProvider>

    init {
        jeiProviders = setOf(
            BerryMutationProvider()
        )
    }
    override fun getPluginUid(): ResourceLocation {
        return ID
    }

    override fun registerCategories(registration: IRecipeCategoryRegistration) {
        jeiProviders.forEach {
            it.registerCategory(registration)
        }
    }

    override fun registerRecipes(registration: IRecipeRegistration) {
        jeiProviders.forEach {
            it.registerRecipes(registration)
        }
    }

    companion object {
        val ID = cobblemonResource("jei_plugin")
    }
}
