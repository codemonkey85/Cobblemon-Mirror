/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex.entry

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.util.adapters.ExpressionLikeAdapter
import com.cobblemon.mod.common.util.adapters.IdentifierAdapter
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType

object DexEntryAdditions : JsonDataRegistry<DexEntryAdditions.DexEntryAddition> {
    override val id = cobblemonResource("dex_entry_additions")
    override val type = PackType.SERVER_DATA

    override val gson: Gson = GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .registerTypeAdapter(ExpressionLike::class.java, ExpressionLikeAdapter)
        .registerTypeAdapter(ResourceLocation::class.java, IdentifierAdapter)
        .create()

    override val typeToken: TypeToken<DexEntryAddition> = TypeToken.get(DexEntryAddition::class.java)
    override val resourcePath = "dex_entry_additions"

    override val observable = SimpleObservable<DexEntryAdditions>()

    val entries = mutableListOf<PokedexEntry>()

    override fun reload(data: Map<ResourceLocation, DexEntryAddition>) {
        data.entries.forEach { (key, addition) ->
            DexEntries.entries[addition.entryId]?.add(addition)
                ?: return@forEach Cobblemon.LOGGER.error("Unable to find dex entry {} to add to from dex entry addition {}", addition.entryId, key) // Skip if the entry doesn't exist
        }
        observable.emit(this)
    }

    override fun sync(player: ServerPlayer) {} // It'd be synced as part of the DexEntries

    class DexEntryAddition {
        val entryId: ResourceLocation = cobblemonResource("some_addition")
        val forms: List<PokedexForm> = emptyList()
        val variations: List<PokedexCosmeticVariation> = emptyList()
    }
}