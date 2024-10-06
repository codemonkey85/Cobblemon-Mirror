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
import com.cobblemon.mod.common.util.readText
import com.cobblemon.mod.common.util.writeText
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.MutableComponent
import java.util.UUID

/**
 * Packet send when a player has requested to join a team. The responsibility
 * of this packet currently is to send a battle challenge message that includes
 * the keybind to challenge them back. In future this is likely to include information
 * about the battle.
 *
 * Handled by [com.cobblemon.mod.common.client.net.battle.TeamRequestNotificationHandler].
 *
 */
class TeamRequestNotificationPacket(
        val teamRequestId: UUID,
        val requesterId: UUID,
        val requesterName: MutableComponent,
): NetworkPacket<TeamRequestNotificationPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(teamRequestId)
        buffer.writeUUID(requesterId)
        buffer.writeText(requesterName)
    }

    companion object {
        val ID = cobblemonResource("team_request_notification")
        fun decode(buffer: RegistryFriendlyByteBuf) = TeamRequestNotificationPacket(buffer.readUUID(), buffer.readUUID(), buffer.readText().copy())
    }
}