/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.api.dialogue.ArtificialDialogueFaceProvider
import com.cobblemon.mod.common.api.dialogue.DialogueFaceProvider
import com.cobblemon.mod.common.api.dialogue.ExpressionLikeDialogueFaceProvider
import com.cobblemon.mod.common.api.dialogue.PlayerDialogueFaceProvider
import com.cobblemon.mod.common.util.asExpressionLike
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import java.lang.reflect.Type

/**
 * Adapter that tries to compose something that will produce a face. JSON primitives and arrays are treated as expressions.
 *
 * If the JSON object has a "type" field with the value "player", it will be deserialized as a [PlayerDialogueFaceProvider].
 *
 * Otherwise, it will be deserialized as an [ArtificialDialogueFaceProvider].
 *
 * @author Hiroku
 * @since January 1st, 2024
 */
object DialogueFaceProviderAdapter : JsonDeserializer<DialogueFaceProvider> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): DialogueFaceProvider {
        return when (json) {
            is JsonPrimitive -> ExpressionLikeDialogueFaceProvider(json.asString.asExpressionLike())
            is JsonArray -> ExpressionLikeDialogueFaceProvider(json.asJsonArray.map { it.asString }.asExpressionLike())
            else -> {
                val jsonObject = json.asJsonObject
                if (jsonObject.get("type")?.asString == "player") {
                    context.deserialize(json, PlayerDialogueFaceProvider::class.java)
                } else {
                    context.deserialize(json, ArtificialDialogueFaceProvider::class.java)
                }
            }
        }
    }
}