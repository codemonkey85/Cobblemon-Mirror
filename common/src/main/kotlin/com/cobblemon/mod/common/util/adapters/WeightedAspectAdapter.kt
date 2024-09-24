/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.api.npc.variation.WeightedAspect
import com.cobblemon.mod.common.util.normalizeToArray
import com.cobblemon.mod.common.util.singularToPluralList
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Shortcut adapter that allows primitive strings to be defined and will assume they are weight-one.
 *
 * @author Hiroku
 * @since August 11th, 2024
 */
object WeightedAspectAdapter : JsonDeserializer<WeightedAspect> {
    override fun deserialize(json: JsonElement, typeOfT: Type, ctx: JsonDeserializationContext): WeightedAspect {
        if (json.isJsonPrimitive) {
            return WeightedAspect(setOf(json.asString), 1.0)
        } else {
            val obj = json.asJsonObject
            obj.singularToPluralList(rootName = "aspect")
            val aspects = obj.get("aspects").normalizeToArray().map { it.asString }.toSet()
            val weight = obj.getAsJsonPrimitive("weight")?.asDouble ?: 1.0
            return WeightedAspect(aspects, weight)
        }
    }
}