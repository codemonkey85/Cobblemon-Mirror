/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex.adapter

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.pokedex.DexData
import com.cobblemon.mod.common.pokedex.DexPokemonData
import com.google.gson.*
import net.minecraft.resources.ResourceLocation
import java.lang.reflect.Type

object DexDataAdapter: JsonDeserializer<DexData> {
    override fun deserialize(jElement: JsonElement, type: Type, context: JsonDeserializationContext): DexData {
        val json = jElement.asJsonObject
        val pokemonList: MutableList<DexPokemonData> = mutableListOf()
        val pokedexListAsJson = json.getAsJsonArray("pokemon_list")
        if(pokedexListAsJson != null){
            for(dexPokemonDataJson in pokedexListAsJson){
                val dexPokemonData = DexPokemonDataAdapter.deserialize(dexPokemonDataJson, DexPokemonData::class.java, context)
                if(dexPokemonData.species != null) {
                    pokemonList.add(dexPokemonData)
                } else {
                    Cobblemon.LOGGER.error("Failed to load Pokedex Entry for {}.", dexPokemonData.identifier)
                }
            }
        }

        val containedDexesArray = json.getAsJsonArray("contained_dexes")
        val containedDexes : MutableList<ResourceLocation> = mutableListOf()
        if(containedDexesArray != null) {
            containedDexes.addAll(containedDexesArray.map { ResourceLocation.tryParse(it.asString)!! })
        }

        return DexData(
            identifier = ResourceLocation.tryParse(json.get("identifier").asString)!!,
            enabled = json.getAsJsonPrimitive("enabled")?.asBoolean ?: true,
            containedDexes = containedDexes,
            pokemonList = pokemonList,
            overrideCategories = json.getAsJsonPrimitive("override_categories")?.asBoolean ?: false
        )
    }
}