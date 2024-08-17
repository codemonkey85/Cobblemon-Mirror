/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import java.util.UUID
import net.minecraft.network.RegistryFriendlyByteBuf

class RemoveSpectatorPacket(val battleId: UUID) : NetworkPacket<RemoveSpectatorPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(battleId)
    }

    companion object {
        val ID = cobblemonResource("remove_spectator")
        fun decode(buffer: RegistryFriendlyByteBuf) = RemoveSpectatorPacket(buffer.readUUID())
    }
}