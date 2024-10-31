/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.interaction

import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer
import kotlin.reflect.KClass
import net.minecraft.resources.ResourceLocation

/**
 * A type adapter for [EntityInteraction]s.
 * For the default implementation with Pokémon entities see [CobblemonPokemonEntityInteractionTypeAdapter].
 *
 * @param T The type of [EntityInteraction].
 *
 * @author Licious
 * @since November 30th, 2022
 */
interface EntityInteractionTypeAdapter<T : EntityInteraction<*>> : JsonDeserializer<T>, JsonSerializer<T> {

    fun registerInteraction(identifier: ResourceLocation, type: KClass<out T>)

}