/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.data

import com.cobblemon.mod.common.api.pokedex.entry.DexEntries
import com.cobblemon.mod.common.api.pokedex.entry.PokedexEntry
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

class DexEntrySyncPacket(dexEntries: Collection<PokedexEntry>) :
    DataRegistrySyncPacket<PokedexEntry, DexEntrySyncPacket>(dexEntries) {
    override fun encodeEntry(buffer: RegistryFriendlyByteBuf, entry: PokedexEntry) {
        entry.encode(buffer)
    }

    override fun decodeEntry(buffer: RegistryFriendlyByteBuf): PokedexEntry? {
        return PokedexEntry.decode(buffer)
    }

    override fun synchronizeDecoded(entries: Collection<PokedexEntry>) {
        DexEntries.reload(entries.associateBy { it.id })
    }

    override val id = ID

    companion object {
        val ID = cobblemonResource("dex_entry_sync")
        fun decode(buffer: RegistryFriendlyByteBuf) = DexEntrySyncPacket(emptyList()).apply { decodeBuffer(buffer) }
    }
}