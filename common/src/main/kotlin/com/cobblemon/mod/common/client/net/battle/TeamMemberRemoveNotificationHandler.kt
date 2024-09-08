/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.battle

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.net.messages.client.battle.TeamMemberRemoveNotificationPacket
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.Minecraft

object TeamMemberRemoveNotificationHandler : ClientNetworkPacketHandler<TeamMemberRemoveNotificationPacket> {
    override fun handle(packet: TeamMemberRemoveNotificationPacket, client: Minecraft) {

        if(packet.teamMemberUUID == client.player?.uuid) {
            // Client removes itself from its team
            val memberCount = CobblemonClient.teamData.multiBattleTeamMembers.count()
            CobblemonClient.teamData.multiBattleTeamMembers.clear()
            val langKey = if(memberCount > 1) "challenge.multi.team_remove.sender" else "challenge.multi.team_disband"
            client.player?.sendSystemMessage(
                lang(
                    langKey,
                )
            )
        } else {
            // Client removes a member from the team
            val memberToRemove = CobblemonClient.teamData.multiBattleTeamMembers.find { it.uuid == packet.teamMemberUUID }
            CobblemonClient.teamData.multiBattleTeamMembers.remove(memberToRemove)

            if(memberToRemove != null) {
                client.player?.sendSystemMessage(
                    lang(
                "challenge.multi.team_remove.receiver",
                        memberToRemove.name,
                    )
                )
            }
        }
    }
}