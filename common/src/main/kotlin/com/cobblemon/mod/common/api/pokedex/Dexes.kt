/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex

import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.pokedex.entry.DexEntries
import com.cobblemon.mod.common.api.pokedex.entry.PokedexEntry
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.net.messages.client.data.DexDefSyncPacket
import com.cobblemon.mod.common.util.adapters.IdentifierAdapter
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType

object Dexes : JsonDataRegistry<PokedexDef> {
    override val id = cobblemonResource("dexes")
    override val type = PackType.SERVER_DATA
    override val observable = SimpleObservable<Dexes>()

    override val gson: Gson = GsonBuilder()
        .registerTypeAdapter(ResourceLocation::class.java, IdentifierAdapter)
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create()

    override val typeToken: TypeToken<PokedexDef> = TypeToken.get(PokedexDef::class.java)
    override val resourcePath = "dexes"

    //Maps a dex id to its PokedexDef
    val dexEntryMap = mutableMapOf<ResourceLocation, PokedexDef>()
    //Map dex id to map of pokedex entry id -> entry map
    val entries = mutableMapOf<ResourceLocation, Map<ResourceLocation, PokedexEntry>>()

    override fun reload(data: Map<ResourceLocation, PokedexDef>) {
        data.forEach { (location, entry) ->
            val entryMap = entry.entries
                .map { DexEntries.entries[it] ?: throw IllegalArgumentException("Unknown dex entry $it") }
                .associateBy { it.id }
            entries[location] = entryMap
        }
        dexEntryMap.putAll(data)
    }

    fun getDexEntryById(dexId: ResourceLocation, entryId: ResourceLocation) = entries[dexId]?.get(entryId)

    override fun sync(player: ServerPlayer) {
        DexDefSyncPacket(dexEntryMap.values)
    }
}