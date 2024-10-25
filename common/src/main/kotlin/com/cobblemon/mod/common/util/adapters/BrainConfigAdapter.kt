/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.api.ai.config.BrainConfig
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

/**
 * Adapter that deserializes a [BrainConfig] from a JSON object. If the JSON is just a string, it assumes
 * that that is the type and the rest of the properties should be default. This is just as shorthand.
 *
 * In general a brain config will be an object with a 'type' property matching something inside [BrainConfig.types].
 *
 * @see BrainConfig
 * @since October 13th, 2024
 * @author Hiroku
 */
object BrainConfigAdapter : JsonDeserializer<BrainConfig> {
    override fun deserialize(json: JsonElement, typeOfT: Type, ctx: JsonDeserializationContext): BrainConfig? {
        if (json.isJsonPrimitive) {
            val type = json.asString
            val clazz = BrainConfig.types[type]
                ?: throw IllegalArgumentException("Unknown brain config type: $type")
            return ctx.deserialize(JsonObject(), clazz)
        } else {
            val obj = json.asJsonObject
            val type = obj.get("type")?.asString
                ?: throw IllegalArgumentException("Missing brain config type. A brain config element must have a 'type' value.")
            val clazz = BrainConfig.types[type]
                ?: throw IllegalArgumentException("Unknown brain config type: $type")
            return ctx.deserialize(obj, clazz)
        }
    }
}