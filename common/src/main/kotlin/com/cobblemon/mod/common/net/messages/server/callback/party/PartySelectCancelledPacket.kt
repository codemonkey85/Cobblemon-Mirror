/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.callback.party

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import java.util.UUID
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Packet sent to the server when the player closed the party selection GUI.
 *
 * @author Hiroku
 * @since July 7th, 2023
 */
class PartySelectCancelledPacket(val uuid: UUID) : NetworkPacket<PartySelectCancelledPacket> {
    companion object {
        val ID = cobblemonResource("party_select_cancelled")
        fun decode(buffer: RegistryFriendlyByteBuf) = PartySelectCancelledPacket(buffer.readUUID())
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(uuid)
    }
}