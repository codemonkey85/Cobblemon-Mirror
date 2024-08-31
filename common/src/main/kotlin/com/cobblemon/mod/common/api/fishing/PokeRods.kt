/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.fishing

import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.net.messages.client.data.PokeRodRegistrySyncPacket
import com.cobblemon.mod.common.util.adapters.IdentifierAdapter
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType

/**
 * The data registry for [PokeRod]s.
 * All the pokerod fields are guaranteed to exist
 */
object PokeRods : JsonDataRegistry<PokeRod> {

    override val id = cobblemonResource("pokerods")
    override val type = PackType.SERVER_DATA
    override val observable = SimpleObservable<PokeRods>()

    // ToDo once datapack pokerod is implemented add required adapters here
    override val gson: Gson = GsonBuilder()
        .disableHtmlEscaping()
        .registerTypeAdapter(ResourceLocation::class.java, IdentifierAdapter)
        .setPrettyPrinting()
        .create()
    override val typeToken: TypeToken<PokeRod> = TypeToken.get(PokeRod::class.java)
    override val resourcePath = "pokerods"

    private val rods = mutableMapOf<ResourceLocation, PokeRod>()

    override fun reload(data: Map<ResourceLocation, PokeRod>) {
        data.forEach {
            it.value.name = it.key
            rods[it.key] = it.value
        }
        this.observable.emit(this)
    }

    override fun sync(player: ServerPlayer) {
        PokeRodRegistrySyncPacket(rods.values).sendToPlayer(player)
    }

    /**
     * Gets a Pokerod from registry name.
     * @return the pokerod object if found otherwise null.
     */
    fun getPokeRod(name : ResourceLocation): PokeRod? = rods[name]

}