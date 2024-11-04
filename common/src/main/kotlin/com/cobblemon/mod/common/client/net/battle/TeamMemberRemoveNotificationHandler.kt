/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.battle

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.api.text.aqua
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.render.ClientPlayerIcon
import com.cobblemon.mod.common.net.messages.client.battle.TeamMemberRemoveNotificationPacket
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.Minecraft

object TeamMemberRemoveNotificationHandler : ClientNetworkPacketHandler<TeamMemberRemoveNotificationPacket> {
    override fun handle(packet: TeamMemberRemoveNotificationPacket, client: Minecraft) {

        if(packet.teamMemberUUID == client.player?.uuid) {
            // Client removes itself from its team
            val members = CobblemonClient.teamData.multiBattleTeamMembers.map { it.uuid }
            val memberCount = members.count()
            CobblemonClient.teamData.multiBattleTeamMembers.clear()
            members.forEach { ClientPlayerIcon.update(it) }
            val langKey = if(memberCount > 1) "team.left.self" else "team.disband"
            client.player?.sendSystemMessage(
                lang(
                    langKey,
                ).red()
            )
        } else {
            // Client removes a member from the team
            val memberToRemove = CobblemonClient.teamData.multiBattleTeamMembers.find { it.uuid == packet.teamMemberUUID } ?: return
            CobblemonClient.teamData.multiBattleTeamMembers.remove(memberToRemove)
            ClientPlayerIcon.update(memberToRemove.uuid)

            client.player?.sendSystemMessage(
                lang(
                    "team.left.other",
                    memberToRemove.name.copy().aqua(),
                ).red()
            )
        }
    }
}