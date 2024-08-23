/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex.entry

import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import kotlin.reflect.KClass

enum class PokedexVariationTypes(
    val typeId: ResourceLocation,
    val decoder: (RegistryFriendlyByteBuf) -> PokedexVariation,
    val type: KClass<out PokedexVariation>
) {
    FORM(BasicPokedexVariation.ID, BasicPokedexVariation.Companion::decode, BasicPokedexVariation::class);

    companion object {
        fun getById(id: ResourceLocation): PokedexVariationTypes? {
            return entries.filter {
                it.typeId == id
            }.firstOrNull()
        }
    }
}