/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.battle.TeamJoinNotificationPacket
import com.cobblemon.mod.common.net.messages.client.battle.TeamMemberAddNotificationPacket
import com.cobblemon.mod.common.net.messages.server.BattleTeamResponsePacket
import com.cobblemon.mod.common.util.battleLang
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.party
import com.cobblemon.mod.common.util.server
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import java.util.*

object TeamRequestResponseHandler : ServerNetworkPacketHandler<BattleTeamResponsePacket> {
    override fun handle(packet: BattleTeamResponsePacket, server: MinecraftServer, player: ServerPlayer) {
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

        when (targetedEntity) {
            is PokemonEntity -> {
                return
            }
            is ServerPlayer -> {
                // Bandaid for odd desync thing with data tracker
                if (player == targetedEntity) {
                    return
                }
                // Check in on team requests, if the other player has requested me, this complete the team up
                val existingRequest = BattleRegistry.multiBattleTeamRequests[targetedEntity.uuid]
                if (existingRequest != null && !existingRequest.isExpired() && existingRequest.requestedPlayerUUID == player.uuid) {

                    if(packet.accept) {
                        if (targetedEntity.party().none()) {
                            player.sendSystemMessage(battleLang("error.no_pokemon_opponent"))
                            targetedEntity.sendSystemMessage(battleLang("error.no_pokemon"))
                            BattleRegistry.removeTeamUpRequest(targetedEntity.uuid, existingRequest.requestID)
                            return
                        }

                        if (BattleRegistry.playerToTeam[player.uuid] != null) {
                            // Player is already a member of team
                            player.sendSystemMessage(lang("challenge.multi.team_reject.existing_team"))
                            return
                        }

                        val existingTeam = BattleRegistry.playerToTeam[targetedEntity.uuid]
                        if (existingTeam != null) {

                            if(existingTeam.teamPlayersUUID.count() >= BattleRegistry.MAX_TEAM_MEMBER_COUNT) {
                                player.sendSystemMessage(lang("challenge.multi.team_reject.max_team_size"))
                                return
                            }

                            // TODO: Better checks to figure out if the uuids are actual players
                            // Join to Existing Team
                            existingTeam.teamPlayersUUID.add(player.uuid)

                            // notify the joiner
                            val joinerPacket = TeamJoinNotificationPacket(
                                    existingTeam.teamPlayersUUID,
                                    existingTeam.teamPlayersUUID.mapNotNull { player.level().getPlayerByUUID(it)?.name?.plainCopy() },
                            )
                            CobblemonNetwork.sendPacketToPlayer(player, joinerPacket)

                            // notify the team
                            val teamNotifyPacket = TeamMemberAddNotificationPacket(
                                    player.uuid,
                                    player.name.copy()
                            )
                            CobblemonNetwork.sendPacketToPlayers(existingTeam.teamPlayersUUID.filter { it != player.uuid && player.level().getPlayerByUUID(it) != null }.
                                map { player.level().getPlayerByUUID(it) as ServerPlayer }, teamNotifyPacket)

                        } else {
                            // Create a new team
                            val team = BattleRegistry.MultiBattleTeam(UUID.randomUUID(), listOf(player.uuid, targetedEntity.uuid).toMutableList())
                            BattleRegistry.playerToTeam[player.uuid] = team
                            BattleRegistry.playerToTeam[targetedEntity.uuid] = team
                            BattleRegistry.multiBattleTeams[team.teamID] = team

                            // Notify the team

                            val joinerPacket = TeamJoinNotificationPacket(
                                    team.teamPlayersUUID,
                                    team.teamPlayersUUID.mapNotNull { player.level().getPlayerByUUID(it)?.name?.plainCopy() },
                            )
                            server()?.let {
                                 it.playerList.getPlayer(targetedEntity.uuid)?.let {
                                     targetedServerPlayer -> CobblemonNetwork.sendPacketToPlayer(targetedServerPlayer, joinerPacket)
                                 }
                            }
                            CobblemonNetwork.sendPacketToPlayer(player, joinerPacket)


                            targetedEntity.sendSystemMessage(lang("challenge.multi.team_request.accept.receiver", player.name).yellow())

                            player.sendSystemMessage(lang("challenge.multi.team_request.accept.sender", targetedEntity.name).yellow())
                        }

                    } else {
                        // Play messages to both sides that the team request was declined
                        targetedEntity.sendSystemMessage(lang("challenge.multi.team_request.decline.receiver", player.name).yellow())
                        player.sendSystemMessage(lang("challenge.multi.team_request.decline.sender", targetedEntity.name).yellow())
                    }
                    BattleRegistry.removeTeamUpRequest(targetedEntity.uuid, existingRequest.requestID)
                }
            }
            else -> {
                // Unrecognized challenge target. NPCs will probably go here.
            }
        }
    }

}