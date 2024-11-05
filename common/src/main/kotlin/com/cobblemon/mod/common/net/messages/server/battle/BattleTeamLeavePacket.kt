/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.battle

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.battles.TeamManager.MultiBattleTeam
import com.cobblemon.mod.common.net.serverhandling.battle.TeamLeaveHandler
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Packet sent when a player requests to leave a [MultiBattleTeam].
 *
 * Handled by [TeamLeaveHandler].
 *
 * @author JazzMcNade
 * @since April 15th, 2024
 */
class BattleTeamLeavePacket() : NetworkPacket<BattleTeamLeavePacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
    }
    companion object {
        val ID = cobblemonResource("battle_team_leave")
        fun decode(buffer: RegistryFriendlyByteBuf) = BattleTeamLeavePacket()
    }
}