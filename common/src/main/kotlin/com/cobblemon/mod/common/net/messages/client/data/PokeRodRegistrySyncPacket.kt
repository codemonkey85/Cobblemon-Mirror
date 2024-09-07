/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.data

import com.cobblemon.mod.common.api.fishing.PokeRod
import com.cobblemon.mod.common.api.fishing.PokeRods
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

class PokeRodRegistrySyncPacket(rods: Collection<PokeRod>) : DataRegistrySyncPacket<PokeRod, PokeRodRegistrySyncPacket>(rods) {
    companion object {
        val ID = cobblemonResource("pokerod_sync")
        fun decode(buffer: RegistryFriendlyByteBuf) = PokeRodRegistrySyncPacket(emptyList()).apply { decodeBuffer(buffer) }
    }

    override val id = ID

    override fun encodeEntry(buffer: RegistryFriendlyByteBuf, entry: PokeRod) {
        entry.encode(buffer)
    }

    override fun decodeEntry(buffer: RegistryFriendlyByteBuf) = PokeRod.decode(buffer)

    override fun synchronizeDecoded(entries: Collection<PokeRod>) {
        PokeRods.reload(entries.associateBy { it.name!! })
    }
}