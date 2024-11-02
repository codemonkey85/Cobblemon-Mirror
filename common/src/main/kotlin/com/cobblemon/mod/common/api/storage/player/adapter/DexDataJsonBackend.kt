/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.player.adapter

import com.cobblemon.mod.common.api.pokedex.PokedexManager
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreType
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreTypes
import com.cobblemon.mod.common.util.adapters.CodecBackedAdapter
import com.cobblemon.mod.common.util.adapters.IdentifierAdapter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.resources.ResourceLocation
import java.util.UUID

/**
 * A [PlayerDataStoreBackend] for [PokedexManager]
 *
 * @author Apion
 * @since February 22, 2024
 */
class DexDataJsonBackend: JsonBackedPlayerDataStoreBackend<PokedexManager>("pokedex", PlayerInstancedDataStoreTypes.POKEDEX) {
    override val gson = GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .registerTypeAdapter(PokedexManager::class.java, CodecBackedAdapter(PokedexManager.CODEC))
        .create()
    override val classToken = TypeToken.get(PokedexManager::class.java)
    override val defaultData = defaultDataFunc

    override fun initialize(store: PokedexManager) {
        store.initialize()
    }

    companion object {
        val defaultDataFunc = { uuid: UUID ->
            PokedexManager(uuid, mutableMapOf())
        }
    }

}