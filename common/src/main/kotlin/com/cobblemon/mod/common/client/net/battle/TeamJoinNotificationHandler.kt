/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.battle

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.ClientMultiBattleTeamMember
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.render.ClientPlayerIcon
import com.cobblemon.mod.common.net.messages.client.battle.TeamJoinNotificationPacket
import net.minecraft.client.Minecraft

object TeamJoinNotificationHandler : ClientNetworkPacketHandler<TeamJoinNotificationPacket> {
    override fun handle(packet: TeamJoinNotificationPacket, client: Minecraft) {
        CobblemonClient.teamData.multiBattleTeamMembers = packet.teamMemberUUIDs.mapIndexed { index, uuid -> ClientMultiBattleTeamMember(uuid, packet.teamMemberNames[index]) }.toMutableList()
        packet.teamMemberUUIDs.forEach { ClientPlayerIcon.update(it) }
    }
}