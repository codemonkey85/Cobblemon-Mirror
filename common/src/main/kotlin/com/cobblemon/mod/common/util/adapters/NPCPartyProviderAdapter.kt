/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.api.npc.NPCPartyProvider
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

object NPCPartyProviderAdapter : JsonDeserializer<NPCPartyProvider> {
    override fun deserialize(json: JsonElement, type: Type, ctx: JsonDeserializationContext): NPCPartyProvider {
        val typeName = if (json.isJsonPrimitive) json.asString else json.asJsonObject.get("type").asString
        return NPCPartyProvider.types[typeName]?.invoke(typeName)?.also { it.loadFromJSON(json) }
            ?: throw IllegalStateException("Unable to find party provider by type: $typeName")
    }
}