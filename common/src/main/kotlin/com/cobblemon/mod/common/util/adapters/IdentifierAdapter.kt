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
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import net.minecraft.resources.ResourceLocation

/**
 * Basic string adapter for [ResourceLocation]s.
 *
 * @author Hiroku
 * @since January 24th, 2022
 */
object IdentifierAdapter : JsonSerializer<ResourceLocation>, JsonDeserializer<ResourceLocation> {
    override fun deserialize(json: JsonElement, type: Type, ctx: JsonDeserializationContext) = ResourceLocation.parse(json.asString)
    override fun serialize(src: ResourceLocation, type: Type, ctx: JsonSerializationContext) = JsonPrimitive(src.toString())
}