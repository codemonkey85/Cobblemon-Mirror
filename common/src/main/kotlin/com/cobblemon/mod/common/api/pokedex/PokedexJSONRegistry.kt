/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.pokedex.adapter.DexDataAdapter
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies.getByIdentifier
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.net.messages.client.data.PokedexSyncPacket
import com.cobblemon.mod.common.pokedex.DexData
import com.cobblemon.mod.common.pokedex.DexPokemonData
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import java.util.*

object PokedexJSONRegistry : JsonDataRegistry<DexData> {
    override val id = cobblemonResource("pokedexes")
    override val type = PackType.SERVER_DATA

    override val gson: Gson = GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .registerTypeAdapter(DexData::class.java, DexDataAdapter)
        .create()

    override val typeToken: TypeToken<DexData> = TypeToken.get(DexData::class.java)
    override val resourcePath = "pokedexes"

    override val observable = SimpleObservable<PokedexJSONRegistry>()

    val dexByIdentifier = mutableMapOf<ResourceLocation, DexData>()
    val dexes: MutableCollection<DexData>
        get() = this.dexByIdentifier.values.toMutableList()

    private var _entries: MutableSet<DexPokemonData>? = null
    val entries: Set<DexPokemonData>
        get() {
            if(_entries != null) return _entries as Set<DexPokemonData>

            _entries = mutableSetOf()

            val categoryEntries: MutableMap<PokedexEntryCategory, MutableSet<DexPokemonData>> = mutableMapOf()
            for (category in PokedexEntryCategory.values()) {
                categoryEntries[category] = mutableSetOf()
            }

            //Step 1: Get all dexes without a parent, so we can iterate over them
            val dexesWithoutParents : MutableList<ResourceLocation> = mutableListOf()
            dexesWithoutParents.addAll(dexByIdentifier.keys)

            for (identifier in dexByIdentifier.keys) {
                if(dexByIdentifier[identifier] == null) continue
                for (childDex in dexByIdentifier[identifier]!!.containedDexes) {
                    dexesWithoutParents.remove(childDex)
                }
            }

            //Step 2: Each parent dex adds all of their pokemon and recursively adds the child dexes as well
            for (dexIdentifier in dexesWithoutParents) {
                dexByIdentifier[dexIdentifier]?.parseEntries(_entries as MutableSet<DexPokemonData>, categoryEntries)
            }

            //Step 3: Add all pokemon in the order of their categories
            for(categories in categoryEntries.values){
                for(entry in categories) {
                    if (_entries!!.contains(entry)){
                        _entries!!.add(_entries!!.find { it == entry }!!.combine(entry))
                    } else {
                        _entries!!.add(entry)
                    }
                }
            }

            return _entries as Set<DexPokemonData>
        }

    private var _skipAutoNumberingIndexes : MutableSet<Int>? = null
    private val skipAutoNumberingIndexes : Set<Int>
        get() {
            if(_skipAutoNumberingIndexes != null) return _skipAutoNumberingIndexes as MutableSet<Int>

            _skipAutoNumberingIndexes = mutableSetOf()

            var index = 0
            for (entry in entries){
                if(entry.skipAutoNumbering){
                    _skipAutoNumberingIndexes!!.add(index)
                }
                index++
            }

            return _skipAutoNumberingIndexes!!
        }

    /**
     * Gets the [ResourceLocation] of the next [DexData] in [dexes] based on the [DexData.order] property.
     *
     * @param currentDex The [ResourceLocation] to search [dexes] for
     * @return The next value
     */
    fun getNextDex(currentDex: ResourceLocation): ResourceLocation {
        val orderedDexes = dexes.sortedBy { it.order }.map { it.identifier }

        return if (orderedDexes.indexOf(currentDex) == orderedDexes.size - 1) {
            orderedDexes.first()
        } else {
            orderedDexes[orderedDexes.indexOf(currentDex) + 1]
        }
    }

    /**
     * Gets the [ResourceLocation] of the previous [DexData] in [orderedDexes]
     *
     * @param currentDex The [ResourceLocation] to search [orderedDexes] for
     * @return The next value
     */
    fun getPreviousDex(currentDex: ResourceLocation): ResourceLocation {
        val orderedDexes = dexes.sortedBy { it.order }.map { it.identifier }
        return if (orderedDexes.indexOf(currentDex) == 0) {
            orderedDexes.last()
        } else {
            orderedDexes[orderedDexes.indexOf(currentDex) - 1]
        }
    }

    /**
     * Finds a dex by the pathname of their [ResourceLocation].
     * This method exists for the convenience of finding Cobble default dexes.
     * This uses [getByIdentifier] using the [Cobblemon.MODID] as the namespace and the [name] as the path.
     *
     * @param name The path of the species asset.
     * @return The [DexData] if existing.
     */
    fun getByName(name: String) = this.getByIdentifier(cobblemonResource(name))

    /**
     * Finds a [DexData] by its unique [ResourceLocation].
     *
     * @param identifier The unique [DexData.identifier] of the [DexData].
     * @return The [DexData] if existing.
     */
    fun getByIdentifier(identifier: ResourceLocation) = this.dexByIdentifier[identifier]

    /**
     * Counts the currently loaded dexes.
     *
     * @return The loaded dex amount.
     */
    fun count() = this.dexByIdentifier.size

    /**
     * Gets a list of dexes in namespace.
     *
     * @return The dex list in namespace.
     */
    fun getDexesInNamespace(namespace: String = Cobblemon.MODID): Collection<DexData> = dexes.filter { it.identifier.namespace == namespace }.toList()

    /**
     * Get a collection of loaded namespaces.
     *
     * @return The collection of loaded namespaces.
     */
    fun getNamespaces() : Collection<String> = dexes.map {it.identifier.namespace}.toSet()

    fun getDexPokemonDataInNamespace(namespace: String = Cobblemon.MODID): Collection<DexPokemonData> {
        val dex = getDexesInNamespace(namespace)
        val speciesList: MutableList<DexPokemonData> = mutableListOf()
        dex.forEach {
            if(it.pokemonList != null) {
                it.pokemonList.forEach { dexPokemonData ->
                    speciesList.add(dexPokemonData)
                }
            }
        }

        return speciesList
    }

    fun getSortedDexData(filters: Collection<EntryFilter>): Collection<DexPokemonData> {
        return entries.filter { pokemon ->
            filters.forEach {filter ->
                if(!filter.filter(pokemon)) return@filter false
            }
            return@filter true
        }.toSet()
    }

    fun getPokemonVisualDexNumber(dexPokemonData: DexPokemonData): String {
        if(dexPokemonData.visualNumber != null) return dexPokemonData.visualNumber!!

        val originalIndex = entries.indexOf(dexPokemonData)
        var dexNumber = originalIndex + 1
        for(index in skipAutoNumberingIndexes) {
            if(index < originalIndex){
                dexNumber--
            }
        }

        return dexNumber.toString()
    }

    override fun reload(data: Map<ResourceLocation, DexData>) {
        this.dexes.clear()
        _entries = null
        _skipAutoNumberingIndexes = null
        data.forEach { (identifier, dexData) ->
            if(dexData.enabled){
                try {
                    dexData.identifier = identifier
                    this.dexByIdentifier[identifier] = dexData
                } catch(e: Exception) {
                    Cobblemon.LOGGER.error("Failed to load {} Pokedex", identifier, e)
                }
            }
        }

        dexes.addAll(dexByIdentifier.values)
    }

    override fun sync(player: ServerPlayer) {
        PokedexSyncPacket(dexes.toList()).sendToPlayer(player)
    }
}