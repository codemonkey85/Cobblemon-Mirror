/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.data

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokedex.PokedexDef
import com.cobblemon.mod.common.api.pokedex.Dexes
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

class DexDefSyncPacket(
    dexes: Collection<PokedexDef>
) : DataRegistrySyncPacket<PokedexDef, DexDefSyncPacket>(dexes) {

    override val id = ID

    override fun encodeEntry(buffer: RegistryFriendlyByteBuf, entry: PokedexDef) {
        try {
            entry.encode(buffer)
        } catch (e: Exception) {
            Cobblemon.LOGGER.error("Caught exception encoding the dex")
        }
    }

    override fun decodeEntry(buffer: RegistryFriendlyByteBuf): PokedexDef? {
        val dexData = PokedexDef()
        return try {
            dexData.decode(buffer)
            dexData
        } catch (e: Exception) {
            Cobblemon.LOGGER.error("Caught exception decoding a dex.", e)
            null
        }
    }

    override fun synchronizeDecoded(entries: Collection<PokedexDef>) {
        Dexes.reload(entries.associateBy { it.id })
    }

    companion object {
        val ID = cobblemonResource("pokedex_sync")
        fun decode(buffer: RegistryFriendlyByteBuf) = DexDefSyncPacket(emptyList()).apply { decodeBuffer(buffer) }
    }
}