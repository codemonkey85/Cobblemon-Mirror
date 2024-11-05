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
import com.cobblemon.mod.common.battles.TeamManager.TeamRequest
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.battle.TeamRequestNotificationPacket
import com.cobblemon.mod.common.net.messages.server.battle.BattleTeamRequestPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

/**
 * Processes a player's interaction request to form a team with another player. If valid, creates a respective [TeamRequest]
 * and sends a [TeamRequestNotificationPacket] to the player to decide upon.
 *
 * @author JazzMcNade
 * @since April 15th, 2024
 */
object TeamRequestHandler : ServerNetworkPacketHandler<BattleTeamRequestPacket> {
    override fun handle(packet: BattleTeamRequestPacket, server: MinecraftServer, player: ServerPlayer) {
        val targetedEntity = player.level().getEntity(packet.targetedEntityId)?.let {
            when (it) {
                is PokemonEntity -> it.owner
                is ServerPlayer -> it
                else -> null
            }
        } ?: return

        if (targetedEntity is ServerPlayer) TeamManager.sendRequest(TeamRequest(player, targetedEntity))
    }
}