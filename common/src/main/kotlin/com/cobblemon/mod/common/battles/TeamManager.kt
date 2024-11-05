/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.api.interaction.RequestManager
import com.cobblemon.mod.common.api.interaction.ServerPlayerActionRequest
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.text.aqua
import com.cobblemon.mod.common.net.messages.client.battle.*
import com.cobblemon.mod.common.util.*
import net.minecraft.server.level.ServerPlayer
import java.util.*

/**
 * Responsible for managing active [MultiBattleTeam]s and the respective [TeamRequest]s that form them.
 *
 * @author Segfault Guy, JazzMcNade
 * @since October 26th, 2024
 */
object TeamManager : RequestManager<TeamManager.TeamRequest>() {

    init {
        register(this)
    }

    const val MAX_TEAM_MEMBER_COUNT = 2

    /**
     * Represents an interaction request between players to form a team.
     *
     * @param sender The player sending this request.
     * @param receiver The player receiving this request.
     * @param expiryTime How long (in seconds) this request is active.
     */
    data class TeamRequest(
        override val sender: ServerPlayer,
        override val receiver: ServerPlayer,
        override val expiryTime: Int = 60
    ) : ServerPlayerActionRequest
    {
        override val key: String = "team"
        override val requestID: UUID = UUID.randomUUID()
    }
    // TODO right now team requests are sent PLAYER-to-PLAYER. May want to redesign once larger teams are implemented? Or have a 'team lead' who is responsible for inviting?

    /**
     * Represents a team for multi battles.
     *
     * @param teamPlayers The players that form this team.
     */
    data class MultiBattleTeam(val teamPlayers: MutableList<ServerPlayer>)
    {
        constructor(vararg teamPlayers: ServerPlayer) : this(teamPlayers.toMutableList())
        val teamID: UUID = UUID.randomUUID()
        val teamPlayersUUID get() = this.teamPlayers.map { it.uuid }
    }

    // Multi-battle teams
    private val playerToTeam = mutableMapOf<UUID, MultiBattleTeam>()
    private val multiBattleTeams = mutableMapOf<UUID, MultiBattleTeam>()

    fun getTeam(player: ServerPlayer) = playerToTeam.get(player.uuid)

    fun getTeam(teamID: UUID) = multiBattleTeams.get(teamID)

    override fun notificationPacket(request: TeamRequest): NetworkPacket<*> = TeamRequestNotificationPacket(request)

    override fun expirationPacket(request: TeamRequest): NetworkPacket<*> = TeamRequestExpiredPacket(request)

    /** Removes the [teamEntry]. */
    fun disbandTeam(teamEntry: MultiBattleTeam) {
        // Remove any outstanding team-up requests from the team members
        val pendingTeamRequests = teamEntry.teamPlayersUUID.mapNotNull { member -> this.getOutboundRequest(member) } // team requests are by player
        pendingTeamRequests.forEach { this.cancelRequest(it) }

        // Decline any inbound requests for the team
        ChallengeManager.getInboundRequests(teamEntry.teamID)?.forEach { ChallengeManager.declineRequest(it) } // multi challenges are by team

        // Remove any outstanding multibattle challenges from the team
        ChallengeManager.getOutboundRequest(teamEntry.teamID)?.let { ChallengeManager.cancelRequest(it) } // multi challenges are by team

        // TODO have a way to get all types of requests targeting a team instead of just querying the ChallengeManger

        // Notify remaining players that the team has been disbanded
        teamEntry.teamPlayersUUID.forEach { member ->
            playerToTeam.remove(member)
            member.getPlayer()?.let { CobblemonNetwork.sendPacketToPlayer(it, TeamMemberRemoveNotificationPacket(it.uuid)) }
        }

        // Disband the team
        multiBattleTeams.remove(teamEntry.teamID)
    }

    /** Removes a [player] from their [MultiBattleTeam]. Will disband the team if the last remaining member. */
    fun removeTeamMember(player: ServerPlayer) {
        val teamEntry = playerToTeam.get(player.uuid) ?: return
        teamEntry.teamPlayers.remove(player)
        playerToTeam.remove(player.uuid)

        val notificationPacket = TeamMemberRemoveNotificationPacket(player.uuid)
        CobblemonNetwork.sendPacketToPlayer(player, notificationPacket)
        // Notify remaining members that the player has left the group
        val teamServerPlayers = teamEntry.teamPlayersUUID.mapNotNull { it.getPlayer() }
        CobblemonNetwork.sendPacketToPlayers(teamServerPlayers, notificationPacket)

        if (teamEntry.teamPlayersUUID.count() == 1)  this.disbandTeam(teamEntry)
    }

    /** Adds [player] as a member of existing [team]. */
    fun joinTeam(player: ServerPlayer, team: MultiBattleTeam) {
        // decline any other incoming requests to team up
        this.getInboundRequests(player.uuid)?.forEach { this.cancelRequest(it) }    // this is guaranteed to not contain the request currently being accepted

        // notify the team
        val teamNotifyPacket = TeamMemberAddNotificationPacket(
            player.uuid,
            player.name.copy()
        )
        CobblemonNetwork.sendPacketToPlayers(team.teamPlayers, teamNotifyPacket)

        team.teamPlayers.add(player)
        playerToTeam.put(player.uuid, team)

        // notify the joiner
        val joinerPacket = TeamJoinNotificationPacket(
            team.teamPlayersUUID,
            team.teamPlayers.mapNotNull { it.name.plainCopy() }
        )
        CobblemonNetwork.sendPacketToPlayer(player, joinerPacket)
    }

    /** Creates and registers a [MultiBattleTeam] from [players]. */
    fun createTeam(vararg players: ServerPlayer): MultiBattleTeam {
        // decline any other incoming requests to team up
        players.forEach {
            this.getInboundRequests(it.uuid)?.forEach { this.cancelRequest(it) }    // this is guaranteed to not contain the request currently being accepted
        }

        val team = MultiBattleTeam(*players)
        this.multiBattleTeams.put(team.teamID, team)

        // Notify the team
        val joinerPacket = TeamJoinNotificationPacket(
            team.teamPlayersUUID,
            players.mapNotNull { it.name.plainCopy() }
        )

        players.forEach {
            this.playerToTeam.put(it.uuid, team)
            CobblemonNetwork.sendPacketToPlayer(it, joinerPacket)
        }

        return team
    }

    override fun onAccept(request: TeamRequest) {
        super.onAccept(request) // notification
        val existingTeam = this.getTeam(request.sender)
        if (existingTeam == null)
            this.createTeam(request.receiver, request.sender)
        else
            this.joinTeam(request.receiver, existingTeam)
    }

    override fun canAccept(request: TeamRequest): Boolean {
        val existingReceiverTeam = this.getTeam(request.receiverID)
        val existingSenderTeam = this.getTeam(request.senderID)
        if (request.sender.party().none()) {
            request.notifySender(true, "error.no_pokemon")
            request.notifyReceiver(true, "error.no_pokemon")
        }
        else if (existingReceiverTeam != null) {
            request.notifySender(true, "error.existing_team.other", request.receiver.name.copy().aqua())
            request.notifyReceiver(true, "error.existing_team.self")
        }
        else if (existingSenderTeam != null && existingSenderTeam.teamPlayersUUID.count() >= MAX_TEAM_MEMBER_COUNT) {
            request.notifySender(true, "error.max_team_size.other", request.receiver.name.copy().aqua())
            request.notifyReceiver(true, "error.max_team_size.self")
        }
        else {
            return true
        }
        return false
    }

    override fun isValidInteraction(player: ServerPlayer, target: ServerPlayer): Boolean = player.canInteractWith(target, Cobblemon.config.tradeMaxDistance)

    override fun onLogoff(sender: ServerPlayer) {
        super.onLogoff(sender)
        // Remove player from any teams they may be a part of
        this.removeTeamMember(sender)
    }
}