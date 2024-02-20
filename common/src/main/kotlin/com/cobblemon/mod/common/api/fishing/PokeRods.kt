/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.fishing

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.fossil.Fossil
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.fishing.PokeRod
import com.cobblemon.mod.common.net.messages.client.data.PokeRodRegistrySyncPacket
import com.cobblemon.mod.common.util.adapters.IdentifierAdapter
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.item.ItemStack
import net.minecraft.resource.ResourceType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

/**
 * The data registry for [PokeRod]s.
 * All the pokerod fields are guaranteed to exist
 */
object PokeRods : JsonDataRegistry<PokeRod> {

    override val id = cobblemonResource("pokerods")
    override val type = ResourceType.SERVER_DATA
    override val observable = SimpleObservable<PokeRods>()

    // ToDo once datapack pokerod is implemented add required adapters here
    override val gson: Gson = GsonBuilder()
        .disableHtmlEscaping()
        .registerTypeAdapter(Identifier::class.java, IdentifierAdapter)
        .setPrettyPrinting()
        .create()
    override val typeToken: TypeToken<PokeRod> = TypeToken.get(PokeRod::class.java)
    override val resourcePath = "pokerods"

    private val rods = mutableMapOf<Identifier, PokeRod>()

    override fun reload(data: Map<Identifier, PokeRod>) {
        {
            data.forEach {
                it.value.name = it.key
                rods[it.key] = it.value
            }
        }
        rods.putAll(data)
        this.observable.emit(this)
    }

    override fun sync(player: ServerPlayerEntity) {
        PokeRodRegistrySyncPacket(rods.values).sendToPlayer(player)
    }

    /**
     * Gets a Pokerod from registry name.
     * @return the pokerod object if found otherwise null.
     */
    fun getPokeRod(name : Identifier): PokeRod? = rods[name]

}