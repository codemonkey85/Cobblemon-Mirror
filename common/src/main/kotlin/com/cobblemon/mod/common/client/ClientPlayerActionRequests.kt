/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client

import com.cobblemon.mod.common.client.battle.ClientBattleChallenge
import com.cobblemon.mod.common.client.particle.BedrockParticleOptionsRepository
import com.cobblemon.mod.common.client.particle.ParticleStorm
import com.cobblemon.mod.common.client.render.MatrixWrapper
import com.cobblemon.mod.common.client.trade.ClientTradeOffer
import com.cobblemon.mod.common.util.cobblemonResource
import java.util.UUID
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3

class ClientPlayerActionRequests {
    val battleChallenges = mutableListOf<ClientBattleChallenge>()
    val multiBattleTeamRequests = mutableListOf<ClientBattleChallenge>()
    val tradeOffers = mutableListOf<ClientTradeOffer>()

    val trackedPlayers = mutableMapOf<UUID, MatrixWrapper>()

    fun onMultiChallengeExpired(challengeId: UUID) {
        val players = multiBattleTeamRequests.filter { it.challengeId == challengeId }.flatMap { it.challengerIds }
        players.forEach(trackedPlayers::remove)
        multiBattleTeamRequests.removeIf { it.challengeId == challengeId }
    }

    fun onChallengeExpired(challengeId: UUID) {
        val players = battleChallenges.filter { it.challengeId == challengeId }.flatMap { it.challengerIds }
        players.forEach(trackedPlayers::remove)
        battleChallenges.removeIf { it.challengeId == challengeId }
    }

    fun onTradeOfferExpired(uuid: UUID) {
        val tradeOffers = tradeOffers.filter { it.tradeOfferId == uuid }
        val players = tradeOffers.map { it.traderId }
        players.forEach(trackedPlayers::remove)
        this.tradeOffers.removeAll(tradeOffers)
    }

    fun onRenderPlayer(player: Player, partialTicks: Float) {
        val matrixWrapper = trackedPlayers[player.uuid] ?: return
        val position = player.getEyePosition(partialTicks)
        matrixWrapper.updatePosition(
            Vec3(
                position.x,
                position.y + 1.1,
                position.z
            )
        )
    }

    fun addParticleEffect(challenge: ClientBattleChallenge) {
        challenge.challengerIds.forEach { playerId ->
            val player = Minecraft.getInstance().level?.getPlayerByUUID(playerId) ?: return@forEach
            val wrapper = trackedPlayers.getOrPut(player.uuid) { MatrixWrapper() }
            wrapper.updatePosition(player.getEyePosition(0F).add(0.0, 1.1, 0.0))
            val effect = BedrockParticleOptionsRepository.getEffect(cobblemonResource("particle_exclamation"))
            if (effect != null) {
                val particleStorm = ParticleStorm(
                    effect = effect,
                    matrixWrapper = wrapper,
                    sourceAlive = { !player.isRemoved && trackedPlayers.containsKey(playerId) },
                    world = Minecraft.getInstance().level ?: return,
                    entity = player,
                )
                particleStorm.spawn()
            }
        }
    }
}