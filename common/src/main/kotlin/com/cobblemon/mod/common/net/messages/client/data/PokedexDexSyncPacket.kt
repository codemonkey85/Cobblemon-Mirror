/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.data

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokedex.def.PokedexDef
import com.cobblemon.mod.common.api.pokedex.Dexes
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

class PokedexDexSyncPacket(
    dexes: Collection<PokedexDef>
) : DataRegistrySyncPacket<PokedexDef, PokedexDexSyncPacket>(dexes) {

    override val id = ID

    override fun encodeEntry(buffer: RegistryFriendlyByteBuf, entry: PokedexDef) {
        PokedexDef.PACKET_CODEC.encode(buffer, entry)
    }

    override fun decodeEntry(buffer: RegistryFriendlyByteBuf): PokedexDef? {
        return PokedexDef.PACKET_CODEC.decode(buffer)
    }

    override fun synchronizeDecoded(entries: Collection<PokedexDef>) {
        Dexes.reload(entries.associateBy { it.id })
    }

    companion object {
        val ID = cobblemonResource("pokedex_sync")
        fun decode(buffer: RegistryFriendlyByteBuf) = PokedexDexSyncPacket(emptyList()).apply { decodeBuffer(buffer) }
    }
}