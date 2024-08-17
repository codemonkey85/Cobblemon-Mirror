/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.pokedex

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

class MapUpdatePacket(val imageBytes: ByteArray) : NetworkPacket<MapUpdatePacket> {
    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeByteArray(imageBytes)
    }

    companion object {
        val ID = cobblemonResource("update_map_packet")

        fun decode(buffer: RegistryFriendlyByteBuf): MapUpdatePacket {
            val imageBytes = buffer.readByteArray()
            return MapUpdatePacket(imageBytes)
        }
    }
}
