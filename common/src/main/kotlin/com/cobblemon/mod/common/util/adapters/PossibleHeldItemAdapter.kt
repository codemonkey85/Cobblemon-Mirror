/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.api.spawning.detail.PossibleHeldItem
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import net.minecraft.core.component.DataComponentMap
import java.lang.reflect.Type

/**
 * A JSON deserializer for [PossibleHeldItem].
 *
 * @author Hiroku
 * @since February 17th, 2023
 */
object PossibleHeldItemAdapter : JsonDeserializer<PossibleHeldItem> {
    override fun deserialize(json: JsonElement, tp: Type, ctx: JsonDeserializationContext): PossibleHeldItem {
        if (json.isJsonPrimitive) {
            return PossibleHeldItem(
                item = json.asString,
                percentage = 100.0
            )
        } else {
            json as JsonObject
            val componentMap = JsonOps.INSTANCE.withDecoder(DataComponentMap.CODEC).apply(json.get("nbt")).getOrThrow {
                return@getOrThrow IllegalStateException("Cant serialize components for held item")
            }.first
            val item = json.get("item").asString
            val percentage = json.get("percentage")?.asDouble ?: 100.0
            return PossibleHeldItem(
                item = item,
                percentage = percentage,
                componentMap = componentMap
            )
        }
    }

}