package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.api.pokedex.entry.PokedexVariation
import com.cobblemon.mod.common.api.pokedex.entry.PokedexVariationTypes
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import net.minecraft.resources.ResourceLocation
import java.lang.reflect.Type

object PokedexVariationAdapter : JsonDeserializer<PokedexVariation> {
    override fun deserialize(
        jElement: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): PokedexVariation {
        val json = jElement.asJsonObject
        val type = PokedexVariationTypes.getById(ResourceLocation.parse(json.get("type").asString))!!
        return context.deserialize(json, type.type.java)
    }
}