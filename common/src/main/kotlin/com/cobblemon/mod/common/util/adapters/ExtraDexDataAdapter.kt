package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.api.dex.entry.ExtraDexData
import com.cobblemon.mod.common.api.dex.entry.ExtraDexDataTypes
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation
import java.lang.reflect.Type

object ExtraDexDataAdapter : JsonDeserializer<ExtraDexData> {
    override fun deserialize(
        jElement: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ExtraDexData {
        val json = jElement.asJsonObject
        val type = ExtraDexDataTypes.getById(ResourceLocation.parse(json.get("type").asString))!!
        return context.deserialize(json, type.type.java)
    }
}