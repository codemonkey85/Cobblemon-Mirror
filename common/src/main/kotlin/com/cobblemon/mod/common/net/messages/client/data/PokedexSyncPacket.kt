/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.data

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokedex.PokedexJSONRegistry
import com.cobblemon.mod.common.pokedex.DexData
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

// We do not need to know every single attribute as a client, as such, we only sync the aspects that matter
class PokedexSyncPacket(dexes: Collection<DexData>) : DataRegistrySyncPacket<DexData, PokedexSyncPacket>(dexes) {

    override val id = ID

    override fun encodeEntry(buffer: RegistryFriendlyByteBuf, entry: DexData) {
        try {
            entry.encode(buffer)
        } catch (e: Exception) {
            Cobblemon.LOGGER.error("Caught exception encoding the dex {}", entry.identifier, e)
        }
    }

    override fun decodeEntry(buffer: RegistryFriendlyByteBuf): DexData? {
        val dexData = DexData(cobblemonResource("dex"))
        return try {
            dexData.decode(buffer)
            dexData
        } catch (e: Exception) {
            Cobblemon.LOGGER.error("Caught exception decoding a dex.", e)
            null
        }
    }

    override fun synchronizeDecoded(entries: Collection<DexData>) {
        PokedexJSONRegistry.reload(entries.associateBy { it.identifier })
    }

    companion object {
        val ID = cobblemonResource("pokedex_sync")
        fun decode(buffer: RegistryFriendlyByteBuf): PokedexSyncPacket = PokedexSyncPacket(emptyList()).apply { decodeBuffer(buffer) }
    }
}