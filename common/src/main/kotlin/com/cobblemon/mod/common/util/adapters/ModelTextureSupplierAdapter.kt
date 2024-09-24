/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.client.render.AnimatedModelTextureSupplier
import com.cobblemon.mod.common.client.render.ModelTextureSupplier
import com.cobblemon.mod.common.client.render.StaticModelTextureSupplier
import com.cobblemon.mod.common.client.render.VariableModelTextureSupplier
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type
import net.minecraft.resources.ResourceLocation

/**
 * Adapter for reading a [ModelTextureSupplier] from JSON. This can be either a simple string field for a
 * [StaticModelTextureSupplier], or an object with specific fields in the case of [AnimatedModelTextureSupplier].
 *
 * @author Hiroku
 * @since February 6th, 2023
 */
object ModelTextureSupplierAdapter : JsonDeserializer<ModelTextureSupplier> {
    override fun deserialize(json: JsonElement, type: Type, ctx: JsonDeserializationContext): ModelTextureSupplier {
        if (json.isJsonPrimitive) {
            if (json.asString == "variable") {
                return VariableModelTextureSupplier()
            }

            return StaticModelTextureSupplier(ResourceLocation.parse(json.asString))
        } else if (json.isJsonObject) {
            val jsonObject = json as JsonObject
            val loop = jsonObject.get("loop")?.asBoolean ?: true
            val fps = jsonObject.get("fps")?.asFloat ?: 1F
            val frames = jsonObject.get("frames")?.asJsonArray?.map { ResourceLocation.parse(it.asString) }
                ?: throw IllegalArgumentException("Animated textures require a 'frames' value.")
            return AnimatedModelTextureSupplier(
                loop = loop,
                fps = fps,
                frames = frames
            )
        } else {
            throw IllegalArgumentException("Invalid JSON provided for model texture, it was of type ${json::class.simpleName} instead of a String or Object.")
        }
    }
}