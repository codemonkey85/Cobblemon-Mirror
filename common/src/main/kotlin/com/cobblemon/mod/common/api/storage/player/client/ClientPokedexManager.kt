/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.player.client

import com.cobblemon.mod.common.api.pokedex.AbstractPokedexManager
import com.cobblemon.mod.common.api.pokedex.SpeciesDexRecord
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreType
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreTypes
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.net.messages.client.SetClientPlayerDataPacket
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

class ClientPokedexManager(
    override val speciesRecords: MutableMap<ResourceLocation, SpeciesDexRecord>,
) : AbstractPokedexManager(), ClientInstancedPlayerData {
    override fun encode(buf: RegistryFriendlyByteBuf) {
        buf.writeMap(
            speciesRecords,
            { _, key -> buf.writeString(key.toString()) },
            { _, value -> value.encode(buf) }
        )
    }

    companion object {
        fun decode(buf: RegistryFriendlyByteBuf): SetClientPlayerDataPacket {
            val map = buf.readMap(
                { buf.readString().asIdentifierDefaultingNamespace() },
                { SpeciesDexRecord().also { it.decode(buf) } }
            )
            return SetClientPlayerDataPacket(PlayerInstancedDataStoreTypes.POKEDEX, ClientPokedexManager(map))
        }

        fun runAction(data: ClientInstancedPlayerData) {
            if (data !is ClientPokedexManager) return
            CobblemonClient.clientPokedexData = data
        }

        fun runIncremental(data: ClientInstancedPlayerData) {
            if (data !is ClientPokedexManager) return
            CobblemonClient.clientPokedexData.speciesRecords.putAll(data.speciesRecords)
            CobblemonClient.clientPokedexData.clearCalculatedValues()
        }
    }
}