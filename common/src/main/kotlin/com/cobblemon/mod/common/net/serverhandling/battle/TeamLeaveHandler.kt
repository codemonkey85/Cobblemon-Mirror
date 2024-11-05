/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.battle

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.battles.TeamManager
import com.cobblemon.mod.common.battles.TeamManager.MultiBattleTeam
import com.cobblemon.mod.common.net.messages.server.battle.BattleTeamLeavePacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

/**
 * Processes a player's request to leave an active [MultiBattleTeam].
 *
 * @author JazzMcNade
 * @since April 15th, 2024
 */
object TeamLeaveHandler : ServerNetworkPacketHandler<BattleTeamLeavePacket> {
    override fun handle(packet: BattleTeamLeavePacket, server: MinecraftServer, player: ServerPlayer) {
        TeamManager.removeTeamMember(player)
    }

}