/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.breeding

import com.cobblemon.mod.common.api.data.DataRegistry
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.util.adapters.IdentifierAdapter
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.server.packs.PackType
import net.minecraft.server.level.ServerPlayer
import net.minecraft.resources.ResourceLocation

object EggPatterns : JsonDataRegistry<EggPattern> {
    override val id = cobblemonResource("egg_patterns")
    override val type = PackType.SERVER_DATA
    override val observable = SimpleObservable<EggPatterns>()
    override val gson: Gson = GsonBuilder()
        .registerTypeAdapter(ResourceLocation::class.java, IdentifierAdapter)
        .create()
    override val typeToken = TypeToken.get(EggPattern::class.java)
    override val resourcePath = "egg_patterns"

    val patternMap = hashMapOf<ResourceLocation, EggPattern>()
    override fun reload(data: Map<ResourceLocation, EggPattern>) {
        patternMap.putAll(data)
    }

    override fun sync(player: ServerPlayer) {
        //TODO: Need to implement
    }
}