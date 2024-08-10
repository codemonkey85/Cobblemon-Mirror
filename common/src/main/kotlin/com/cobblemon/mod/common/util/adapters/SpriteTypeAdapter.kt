package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.api.pokemon.egg.EggGroup
import com.cobblemon.mod.common.client.render.SpriteType
import com.google.gson.*
import java.lang.reflect.Type

object SpriteTypeAdapter : JsonDeserializer<SpriteType>, JsonSerializer<SpriteType> {

    // Safe to just cache
    private val spriteTypes = SpriteType.values()
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): SpriteType {
        val rawID = json.asString
        return this.spriteTypes.firstOrNull { spriteType -> spriteType.name.equals(rawID, true) }
            ?: throw IllegalStateException("Failed to resolve sprite type from: $rawID")
    }

    override fun serialize(src: SpriteType, typeOfSrc: Type, context: JsonSerializationContext): JsonElement = JsonPrimitive(src.name.lowercase())
}