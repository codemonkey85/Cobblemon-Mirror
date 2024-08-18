/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokedex

import com.cobblemon.mod.common.api.data.ClientDataSynchronizer
import com.cobblemon.mod.common.api.pokedex.PokedexEntryCategory
import com.cobblemon.mod.common.api.pokedex.PokedexJSONRegistry
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.writeIdentifier
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

class DexData (
    var identifier : ResourceLocation,
    var enabled : Boolean = true,
    var order: Int = 0,
    var containedDexes : MutableList<ResourceLocation> = mutableListOf(),
    var pokemonList : MutableList<DexPokemonData> = mutableListOf(),
    var overrideCategories : Boolean = false
): ClientDataSynchronizer<DexData> {
    override fun shouldSynchronize(other: DexData): Boolean {
        return other.identifier != identifier || other.containedDexes != containedDexes || other.pokemonList != pokemonList
    }

    fun parseEntries(entries: MutableSet<DexPokemonData>, categoryEntries : MutableMap<PokedexEntryCategory, MutableSet<DexPokemonData>>){
        if (!enabled) return

        for(pokemon in pokemonList){
            if(overrideCategories){
                entries.add(pokemon)
            } else {
                categoryEntries[pokemon.category]!!.add(pokemon)
            }
        }

        for(childDexIdentifier in containedDexes){
            PokedexJSONRegistry.dexByIdentifier
            PokedexJSONRegistry.getByIdentifier(childDexIdentifier)?.parseEntries(entries, categoryEntries)
        }
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeIdentifier(identifier)
        buffer.writeBoolean(overrideCategories)
        buffer.writeBoolean(enabled)
        buffer.writeInt(containedDexes.size)
        containedDexes.forEach {
            buffer.writeIdentifier(it)
        }
        buffer.writeInt(pokemonList.size)
        pokemonList.forEach {
            it.encode(buffer)
        }
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        identifier = buffer.readIdentifier()
        overrideCategories = buffer.readBoolean()
        enabled = buffer.readBoolean()
        val subdexesSize = buffer.readInt()
        for (i in 0 until subdexesSize){
            containedDexes.add(buffer.readIdentifier())
        }
        val pokemonSize = buffer.readInt()
        for (i in 0 until pokemonSize){
            val decodedPokemon = DexPokemonData()
            decodedPokemon.decode(buffer)
            pokemonList.add(decodedPokemon)
        }
    }
}