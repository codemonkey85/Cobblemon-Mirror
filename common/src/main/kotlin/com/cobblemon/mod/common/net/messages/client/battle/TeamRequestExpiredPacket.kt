/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

/**
 * Packet fired to tell the client that a team request expired.
 *
 * @author JazzMcNade
 * @since April 4th, 2024
 */
class TeamRequestExpiredPacket(val teamRequestId: UUID) : NetworkPacket<TeamRequestExpiredPacket> {
    companion object {
        val ID = cobblemonResource("team_request_expired")
        fun decode(buffer: RegistryFriendlyByteBuf) = TeamRequestExpiredPacket(buffer.readUUID())
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(teamRequestId)
    }
}