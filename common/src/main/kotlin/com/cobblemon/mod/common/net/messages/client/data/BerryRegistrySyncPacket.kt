/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.data

import com.cobblemon.mod.common.api.berry.Berries
import com.cobblemon.mod.common.api.berry.Berry
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.PacketByteBuf

class BerryRegistrySyncPacket(berries: Collection<Berry>) : DataRegistrySyncPacket<Berry, BerryRegistrySyncPacket>(berries) {
    companion object {
        val ID = cobblemonResource("berry_sync")
        fun decode(buffer: RegistryByteBuf) = BerryRegistrySyncPacket(emptyList()).apply { decodeBuffer(buffer) }
    }

    override val id = ID
    override fun encodeEntry(buffer: RegistryByteBuf, entry: Berry) {
        entry.encode(buffer)
    }

    override fun decodeEntry(buffer: RegistryByteBuf): Berry? = Berry.decode(buffer)

    override fun synchronizeDecoded(entries: Collection<Berry>) {
        Berries.reload(entries.associateBy { it.identifier })
    }
}