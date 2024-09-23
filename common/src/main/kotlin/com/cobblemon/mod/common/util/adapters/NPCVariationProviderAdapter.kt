/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.api.npc.variation.NPCVariationProvider
import com.cobblemon.mod.common.api.npc.variation.WeightedAspect
import com.cobblemon.mod.common.api.npc.variation.WeightedNPCVariationProvider
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Standard map adapter for [NPCVariationProvider]. If the value is an array it will assume that
 * it's a [WeightedNPCVariationProvider] for convenience's sake.
 *
 * @author Hiroku
 * @since August 11th, 2024
 */
object NPCVariationProviderAdapter : JsonDeserializer<NPCVariationProvider> {
    override fun deserialize(json: JsonElement, typeOfT: Type, ctx: JsonDeserializationContext): NPCVariationProvider {
        if (json.isJsonArray) {
            val provider = WeightedNPCVariationProvider()
            provider.options = json.asJsonArray.map { ctx.deserialize(it, WeightedAspect::class.java) }
            return provider
        }

        val type = json.asJsonObject.get("type").asString
        val clazz = NPCVariationProvider.types[type] ?: throw IllegalArgumentException("Unknown NPCVariationProvider type: $type")
        return ctx.deserialize(json, clazz)
    }
}