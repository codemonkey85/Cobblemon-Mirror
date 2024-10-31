/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type
import net.minecraft.world.phys.AABB

object BoxAdapter : JsonDeserializer<AABB> {
    override fun deserialize(json: JsonElement, type: Type, ctx: JsonDeserializationContext): AABB {
        json as JsonObject
        return AABB(
            json.get("minX")?.asDouble ?: -9999999.0,
            json.get("minY")?.asDouble ?: 0.0,
            json.get("minZ")?.asDouble ?: -9999999.0,
            json.get("maxX")?.asDouble ?: 9999999.0,
            json.get("maxY")?.asDouble ?: 9999999.0,
            json.get("maxZ")?.asDouble ?: 9999999.0
        )
    }
}