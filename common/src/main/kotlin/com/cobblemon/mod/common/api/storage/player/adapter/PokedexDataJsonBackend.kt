/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.player.adapter

import com.cobblemon.mod.common.api.pokedex.PokedexRecord
import com.cobblemon.mod.common.api.pokedex.adapter.GlobalTrackedDataAdapter
import com.cobblemon.mod.common.api.pokedex.adapter.PokedexInstanceCreator
import com.cobblemon.mod.common.api.pokedex.trackeddata.GlobalTrackedData
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreType
import com.cobblemon.mod.common.util.adapters.IdentifierAdapter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.resources.ResourceLocation
import java.util.UUID

/**
 * A [PlayerDataStoreBackend] for [PokedexPlayerData]
 *
 * @author Apion
 * @since February 22, 2024
 */
class PokedexDataJsonBackend: JsonBackedPlayerDataStoreBackend<PokedexRecord>("pokedex", PlayerInstancedDataStoreType.POKEDEX) {
    override val gson = GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .registerTypeAdapter(ResourceLocation::class.java, IdentifierAdapter)
        .registerTypeAdapter(GlobalTrackedData::class.java, GlobalTrackedDataAdapter)
        .registerTypeAdapter(PokedexRecord::class.java, PokedexInstanceCreator)
        .create()
    override val classToken = TypeToken.get(PokedexRecord::class.java)
    override val defaultData = defaultDataFunc

    companion object {
        val defaultDataFunc = { uuid: UUID ->
            PokedexRecord(uuid)
        }
    }

}