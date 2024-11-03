/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.api.ai.config.task.TaskConfig
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

/**
 * Adapter that deserializes a [TaskConfig] from a JSON object. If the JSON is just a string, it assumes
 * that that is the type and the rest of the properties should be default. This is just as shorthand.
 *
 * In general a task config will be an object with a 'type' property matching something inside [TaskConfig.types].
 *
 * @see TaskConfig
 * @since October 14th, 2024
 * @author Hiroku
 */
object TaskConfigAdapter : JsonDeserializer<TaskConfig> {
    override fun deserialize(json: JsonElement, typeOfT: Type, ctx: JsonDeserializationContext): TaskConfig? {
        if (json.isJsonPrimitive) {
            val type = json.asString
            val clazz = TaskConfig.types[type]
                ?: throw IllegalArgumentException("Unknown task config type: $type")
            return ctx.deserialize(JsonObject(), clazz)
        } else {
            val obj = json.asJsonObject
            val type = obj.get("type")?.asString
                ?: throw IllegalArgumentException("Missing task config type. A task config element must have a 'type' value.")
            val clazz = TaskConfig.types[type]
                ?: throw IllegalArgumentException("Unknown task config type: $type")
            return ctx.deserialize(obj, clazz)
        }
    }
}