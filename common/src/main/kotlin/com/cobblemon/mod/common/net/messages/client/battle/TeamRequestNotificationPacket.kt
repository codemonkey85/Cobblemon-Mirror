/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.battles.TeamManager
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

/**
 * Packet sent when a player has requested to form or join a team.
 *
 * Handled by [com.cobblemon.mod.common.client.net.battle.TeamRequestNotificationHandler].
 *
 * @param requestID The unique identifier of the request.
 * @param senderID The unique identifier of the party that sent the request.
 * @param expiryTime How long (in seconds) this request is active.
 *
 * @author JazzMcNade
 * @since April 15th, 2024
 */
class TeamRequestNotificationPacket(
    val requestID: UUID,
    val senderID: UUID,
    val expiryTime: Int
): NetworkPacket<TeamRequestNotificationPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(requestID)
        buffer.writeUUID(senderID)
        buffer.writeInt(expiryTime)
    }

    companion object {
        val ID = cobblemonResource("team_request_notification")
        fun decode(buffer: RegistryFriendlyByteBuf) = TeamRequestNotificationPacket(
            buffer.readUUID(),
            buffer.readUUID(),
            buffer.readInt()
        )
    }

    constructor(request: TeamManager.TeamRequest) : this(
        requestID = request.requestID,
        senderID = request.senderID,
        expiryTime = request.expiryTime
    )
}