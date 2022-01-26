package com.cablemc.pokemoncobbled.common.api.pokemon.effect.adapter

import com.cablemc.pokemoncobbled.common.api.pokemon.effect.ShoulderEffect
import com.cablemc.pokemoncobbled.common.api.pokemon.effect.ShoulderEffectRegistry
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

object ShoulderEffectAdapter: JsonDeserializer<ShoulderEffect>, JsonSerializer<ShoulderEffect> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ShoulderEffect {
        return ShoulderEffectRegistry.get(json.asString) ?: throw NoSuchElementException("Error reading ShouldEffect ${json.asString}")
    }

    override fun serialize(src: ShoulderEffect, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(src.name)
    }
}