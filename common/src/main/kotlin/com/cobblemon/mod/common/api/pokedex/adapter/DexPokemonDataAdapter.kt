/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex.adapter

import com.cobblemon.mod.common.api.pokedex.PokedexEntryCategory
import com.cobblemon.mod.common.pokedex.DexPokemonData
import com.google.gson.*
import net.minecraft.resources.ResourceLocation
import java.lang.reflect.Type

object DexPokemonDataAdapter: JsonDeserializer<DexPokemonData> {
    override fun deserialize(jElement: JsonElement, type: Type, context: JsonDeserializationContext): DexPokemonData {
        val json = jElement.asJsonObject
        val categoryJson = json.get("category")
        var category = PokedexEntryCategory.STANDARD
        if(categoryJson != null){
            category = PokedexEntryCategory.from(categoryJson.asString) ?: category
        }

        return DexPokemonData(
            identifier = ResourceLocation.tryParse(json.get("identifier").asString)!!,
            forms = json.getAsJsonArray("forms")?.map { it.asString }?.toMutableList() ?: mutableListOf(),
            category = category,
            tags = json.getAsJsonArray("tags")?.map { it.asString }?.toMutableList() ?: mutableListOf(),
            invisibleUntilFound = json.getAsJsonPrimitive("invisible")?.asBoolean == true,
            visualNumber = json.getAsJsonPrimitive("visual_number")?.asString,
            skipAutoNumbering = json.getAsJsonPrimitive("skip_auto_numbering")?.asBoolean == true
        )
    }

}