/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.api.scheduling.afterOnServer
import com.cobblemon.mod.common.api.text.aqua
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.battle.TeamRequestNotificationPacket
import com.cobblemon.mod.common.net.messages.server.BattleTeamRequestPacket
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.traceFirstEntityCollision
import java.util.UUID
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.ClipContext

object TeamRequestHandler : ServerNetworkPacketHandler<BattleTeamRequestPacket> {
    override fun handle(packet: BattleTeamRequestPacket, server: MinecraftServer, player: ServerPlayer) {
        if(player.isSpectator) return

        val targetedEntity = player.level().getEntity(packet.targetedEntityId)?.let {
            if (it is PokemonEntity) {
                val owner = it.owner
                if (owner != null) {
                    return@let owner
                }
            }
            return@let it
        } ?: return

        // Check los and range
        if (player.traceFirstEntityCollision(
            entityClass = LivingEntity::class.java,
            ignoreEntity = player,
            maxDistance = Cobblemon.config.BattlePvPMaxDistance,
            collideBlock = ClipContext.Fluid.NONE) != targetedEntity) {
            player.sendSystemMessage(lang("ui.interact.failed").yellow())
            return
        }

        when (targetedEntity) {
            is PokemonEntity -> {
                return
            }
            is ServerPlayer -> {
                // Bandaid for odd desync thing with data tracker
                if (player == targetedEntity) {
                    return
                }
                // check for team
                val existingPlayerTeam = BattleRegistry.playerToTeam[player.uuid]
                val existingTargetTeam = BattleRegistry.playerToTeam[targetedEntity.uuid]
                val existingPlayerTeamRequest = BattleRegistry.multiBattleTeamRequests[player.uuid]
                val existingTargetTeamRequest = BattleRegistry.multiBattleTeamRequests[targetedEntity.uuid]
                // TODO: Allow for teams of size > 2
                if (existingPlayerTeam == null && existingTargetTeam == null && existingPlayerTeamRequest == null && existingTargetTeamRequest == null) {
                    // Make a request to the target to join a team
                    val requestId = UUID.randomUUID()
                    val teamRequest = BattleRegistry.TeamRequest(requestId, targetedEntity.uuid)
                    BattleRegistry.multiBattleTeamRequests[player.uuid] = teamRequest
                    player.sendSystemMessage(lang("challenge.multi.team_request.sender", targetedEntity.name))
                    targetedEntity.sendPacket(TeamRequestNotificationPacket(requestId, player.uuid, player.name.copy().aqua()))
                    // Add timeout callback to cancel the request
                    afterOnServer(seconds = teamRequest.expiryTimeSeconds.toFloat()) {
                        BattleRegistry.removeTeamUpRequest(player.uuid, requestId = teamRequest.requestID)
                    }
                }
            }
            else -> {
                // Unrecognized challenge target. NPCs will probably go here.
            }
        }
    }
}