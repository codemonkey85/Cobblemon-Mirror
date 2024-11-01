/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.battles.TeamManager.TeamRequest
import com.cobblemon.mod.common.net.messages.client.battle.TeamRequestNotificationPacket
import com.cobblemon.mod.common.net.serverhandling.battle.TeamRequestResponseHandler
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.*

/**
 * Packet sent when a player responds to a [TeamRequest] after receiving the respective [TeamRequestNotificationPacket].
 *
 * Handled by [TeamRequestResponseHandler].
 *
 * @param requestID The unique identifier of the request that the player is responding to.
 * @param accept Whether the player accepted the team request.
 *
 * @author JazzMcNade
 * @since July 7th, 2024
 */
class BattleTeamResponsePacket(val targetedEntityId: Int, val requestID: UUID, val accept: Boolean) : NetworkPacket<BattleTeamResponsePacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeInt(targetedEntityId)
        buffer.writeUUID(requestID)
        buffer.writeBoolean(accept)
    }
    companion object {
        val ID = cobblemonResource("battle_team_request_response")
        fun decode(buffer: RegistryFriendlyByteBuf) = BattleTeamResponsePacket(buffer.readInt(), buffer.readUUID(), buffer.readBoolean())
    }
}