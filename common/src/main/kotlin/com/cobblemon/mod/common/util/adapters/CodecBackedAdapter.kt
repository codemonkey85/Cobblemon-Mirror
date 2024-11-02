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
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import java.lang.reflect.Type

class CodecBackedAdapter<T>(val codec: Codec<T>) : JsonDeserializer<T>, JsonSerializer<T> {
    override fun deserialize(
        jElement: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): T {
        return JsonOps.INSTANCE.withDecoder(codec).apply(jElement).result().get().first as T
    }

    override fun serialize(
        src: T,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonOps.INSTANCE.withEncoder(codec).apply(src).result().get()
    }
}