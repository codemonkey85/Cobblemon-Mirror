package com.cobblemon.mod.common.util.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import java.lang.reflect.Type

class CodecBackedAdapter<T>(val codec: Codec<T>) : JsonDeserializer<T> {
    override fun deserialize(
        jElement: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): T {
        return JsonOps.INSTANCE.withDecoder(codec).apply(jElement).result().get().first as T
    }

}