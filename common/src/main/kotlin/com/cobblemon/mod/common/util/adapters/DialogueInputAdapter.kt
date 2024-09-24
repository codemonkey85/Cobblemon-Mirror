/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.api.dialogue.DialogueAction
import com.cobblemon.mod.common.api.dialogue.input.DialogueAutoContinueInput
import com.cobblemon.mod.common.api.dialogue.input.DialogueInput
import com.cobblemon.mod.common.api.dialogue.input.DialogueNoInput
import com.cobblemon.mod.common.api.dialogue.input.DialogueOptionSetInput
import com.cobblemon.mod.common.api.dialogue.input.DialogueTextInput
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

object DialogueInputAdapter : JsonDeserializer<DialogueInput> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): DialogueInput {
        if (json.isJsonPrimitive || json.isJsonArray) {
            return DialogueNoInput(action = context.deserialize(json, DialogueAction::class.java))
        }

        val obj = json.asJsonObject
        val typeId = obj.get("type").asString
        return when (typeId) {
            "text" -> context.deserialize(obj, DialogueTextInput::class.java)
            "auto-continue" -> context.deserialize(obj, DialogueAutoContinueInput::class.java)
            "option" -> context.deserialize(obj, DialogueOptionSetInput::class.java)
            "none" -> context.deserialize(obj, DialogueNoInput::class.java)
            else -> throw JsonParseException("Unknown dialogue input type $typeId")
        }
    }
}