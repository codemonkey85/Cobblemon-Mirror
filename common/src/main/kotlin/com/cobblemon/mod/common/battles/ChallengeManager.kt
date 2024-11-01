package com.cobblemon.mod.common.battles

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.interaction.RequestManager
import com.cobblemon.mod.common.api.interaction.ServerPlayerActionRequest
import com.cobblemon.mod.common.api.interaction.ServerTeamActionRequest
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.battles.TeamManager.MAX_TEAM_MEMBER_COUNT
import com.cobblemon.mod.common.battles.TeamManager.MultiBattleTeam
import com.cobblemon.mod.common.net.messages.client.battle.BattleChallengeExpiredPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleChallengeNotificationPacket
import com.cobblemon.mod.common.util.canInteractWith
import com.cobblemon.mod.common.util.getBattleTeam
import com.cobblemon.mod.common.util.party
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3
import java.time.Instant
import java.util.*

/**
 * Responsible for managing [BattleChallenge]s and initiating PVP [PokemonBattle]s.
 *
 * @author Segfault Guy, JazzMcNade
 * @since October 28th, 2024
 */
object ChallengeManager : RequestManager<ChallengeManager.BattleChallenge>("challenge") {

    init {
        register(this)
    }

    const val MAX_BATTLE_RADIUS = 15.0

    /** Mapping of players to their lead Pokemon. */
    private val selectedLead = mutableMapOf<UUID, UUID>()

    /**
     * Represents an interaction request between players or teams to start a battle.
     *
     * @param sender The player sending this request.
     * @param receiver The player receiving this request.
     * @param selectedPokemonId
     * @param battleFormat
     * @param expiryTime How long (in seconds) this request is active.
     */
    abstract class BattleChallenge : ServerPlayerActionRequest {
        abstract val selectedPokemonId: UUID
        abstract val battleFormat: BattleFormat
        override val requestID: UUID = UUID.randomUUID()
        val challengedTime = Instant.now()
        fun isExpired() = Instant.now().isAfter(challengedTime.plusSeconds(expiryTime.toLong()))    // TODO this won't work with recent tickmanager changes
    }

    /** PLAYER-to-PLAYER BattleChallenge */
    data class SinglesBattleChallenge(
        override val sender: ServerPlayer,
        override val receiver: ServerPlayer,
        override val selectedPokemonId: UUID,
        override val battleFormat: BattleFormat,
        override val expiryTime: Int = 20
    ) : BattleChallenge()

    /** TEAM-to-TEAM BattleChallenge */
    data class MultiBattleChallenge(
        override val sender: ServerPlayer,
        override val receiver: ServerPlayer,
        override val selectedPokemonId: UUID,
        override val battleFormat: BattleFormat,    // TODO force this to be multi battletype
        override val expiryTime: Int = 20
    ) : BattleChallenge(), ServerTeamActionRequest {
        override val senderTeam: MultiBattleTeam = sender.getBattleTeam() ?: throw IllegalArgumentException("Sending player is not part of a team!")
        override val receiverTeam: MultiBattleTeam = receiver.getBattleTeam() ?: throw IllegalArgumentException("Target player is not part of a team!")
    }

    override fun expirationPacket(request: BattleChallenge): NetworkPacket<*> = BattleChallengeExpiredPacket(request)

    override fun notificationPacket(request: BattleChallenge): NetworkPacket<*> = BattleChallengeNotificationPacket(request)

    override fun onAccept(request: BattleChallenge) {
        if (request is MultiBattleChallenge) {
            val players = request.receiverTeam.teamPlayers + request.senderTeam.teamPlayers
            val leadPokemon = players.mapNotNull { it.party().first().uuid }
            BattleBuilder.pvp2v2(
                players,
                leadPokemon,
                request.battleFormat
            ).ifErrored {
                erredStart -> erredStart.sendTo(players) { it.red() }
            }
        }
        else {
            BattleBuilder.pvp1v1(
                request.receiver,
                request.sender,
                selectedLead.get(request.receiver.uuid),
                selectedLead.get(request.sender.uuid),
                request.battleFormat
            ).ifErrored {
                it.sendTo(request.receiver) { it.red() }
                it.sendTo(request.sender) { it.red() }
            }
        }
    }

    override fun isValidInteraction(player: ServerPlayer, target: ServerPlayer) = player.canInteractWith(target, Cobblemon.config.BattlePvPMaxDistance)

    override fun canAccept(request: BattleChallenge): Boolean {
        if (request.battleFormat.battleType == BattleTypes.MULTI) {
            val existingReceiverTeam = TeamManager.getTeam(request.receiverID)
            val existingSenderTeam = TeamManager.getTeam(request.senderID)
            // validate teams
            if (existingReceiverTeam == null || existingSenderTeam == null) {
                request.notifySender(true, "$langKey.error.missing_team")
                request.notifyReceiver(true, "$langKey.error.missing_team")
                return false;
            }

            val players = existingReceiverTeam.teamPlayers + existingSenderTeam.teamPlayers
            val leads = players.mapNotNull { it.party().first().uuid }
            // validate team sizes
            if (players.count() != MAX_TEAM_MEMBER_COUNT * 2) {
                request.notifySender(true, "$langKey.error.invalid_team_size", MAX_TEAM_MEMBER_COUNT)
                request.notifyReceiver(true, "$langKey.error.invalid_team_size", MAX_TEAM_MEMBER_COUNT)
            }
            // validate parties
            else if (leads.count() != players.count()) {
                request.notifySender(true, "$langKey.error.missing_pokemon")
                request.notifyReceiver(true, "$langKey.error.missing_pokemon")
            }
            // validate dimension
            else if (this.validateDimension(players)) {
                request.notifySender(true, "battle.error.player_different_dimension")
                request.notifyReceiver(true, "battle.error.player_different_dimension")
            }
            // validate proximity
            else if (this.validateProximity(players)) {
                request.notifySender(true, "battle.error.player_distance") //personal
                request.notifyReceiver(true, "battle.error.player_distance")
            }
            else return true
        }
        else {
            if (request.receiver.party().none()) {
                request.notifySender(true, "$langKey.error.missing_pokemon.self")
                request.notifyReceiver(true, "$langKey.error.missing_pokemon")
            }
            else if (request.sender.party().none()) {
                request.notifySender(true, "$langKey.error.missing_pokemon.personal")
                request.notifyReceiver(true, "$langKey.error.missing_pokemon")
            }
            else return true
        }
        return false
    }

    private fun validateDimension(players: Collection<ServerPlayer>): Boolean {
        val dimension = players.first().level().dimension()
        val playerInWrongDimension = players.firstOrNull { it.level().dimension() != dimension }
        return playerInWrongDimension == null
    }

    private fun validateProximity(players: Collection<ServerPlayer>): Boolean {
        // Check if all players are nearby
        var averagePos = Vec3(0.0, 0.0, 0.0)
        players.forEach { averagePos = averagePos.add(it.position().multiply(1.0 / players.count(), 0.0, 1.0 / players.count())) }
        val farAwayPlayer = players.firstOrNull { it.position().subtract(0.0, it.position().y, 0.0).distanceToSqr(averagePos) > MAX_BATTLE_RADIUS * MAX_BATTLE_RADIUS }
        return farAwayPlayer == null
    }

    fun setLead(player: ServerPlayer, lead: UUID) = this.selectedLead.put(player.uuid, lead)
}