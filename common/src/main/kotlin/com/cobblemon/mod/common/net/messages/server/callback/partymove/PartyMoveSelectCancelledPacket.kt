/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.callback.partymove

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import java.util.UUID
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Packet sent to the server when the player closed the party move selection GUI.
 *
 * @author Hiroku
 * @since July 29th, 2023
 */
class PartyMoveSelectCancelledPacket(val uuid: UUID) : NetworkPacket<PartyMoveSelectCancelledPacket> {
    companion object {
        val ID = cobblemonResource("party_move_select_cancelled")
        fun decode(buffer: RegistryFriendlyByteBuf) = PartyMoveSelectCancelledPacket(buffer.readUUID())
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(uuid)
    }
}