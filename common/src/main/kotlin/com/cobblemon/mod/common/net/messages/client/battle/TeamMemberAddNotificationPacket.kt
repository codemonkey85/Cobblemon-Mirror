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
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.MutableComponent
import java.util.UUID

/**
 * Packet sent when a player joins or leaves a team that the client is currently a member of.
 *
 * Handled by [com.cobblemon.mod.common.client.net.battle.TeamRequestNotificationHandler].
 *
 * @author JazzMcNade
 * @since April 9th, 2024
 */
class TeamMemberAddNotificationPacket(
        val teamMemberUUID: UUID,
        val teamMemberName: MutableComponent,
): NetworkPacket<TeamMemberAddNotificationPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(teamMemberUUID)
        buffer.writeText(teamMemberName)
    }

    companion object {
        val ID = cobblemonResource("team_member_add_notification")
        fun decode(buffer: RegistryFriendlyByteBuf) = TeamMemberAddNotificationPacket(buffer.readUUID(), buffer.readText().copy())
    }
}