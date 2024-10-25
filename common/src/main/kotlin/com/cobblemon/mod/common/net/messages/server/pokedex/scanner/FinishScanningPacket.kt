/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.pokedex.scanner

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

class FinishScanningPacket(val targetedId: Int, val zoomLevel: Int) : NetworkPacket<FinishScanningPacket> {
    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeInt(targetedId)
        buffer.writeInt(zoomLevel)
    }

    companion object {
        val ID = cobblemonResource("finish_scanning_packet")

        fun decode(buffer: RegistryFriendlyByteBuf): FinishScanningPacket {
            val targetId = buffer.readInt()
            val zoomAmount = buffer.readInt()
            return FinishScanningPacket(targetId, zoomAmount)
        }
    }
}