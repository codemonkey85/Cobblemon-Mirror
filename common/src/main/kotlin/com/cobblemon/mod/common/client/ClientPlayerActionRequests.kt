/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client

import com.cobblemon.mod.common.api.snowstorm.BedrockParticleOptions
import com.cobblemon.mod.common.client.battle.ClientBattleChallenge
import com.cobblemon.mod.common.client.particle.BedrockParticleOptionsRepository
import com.cobblemon.mod.common.client.particle.ParticleStorm
import com.cobblemon.mod.common.client.render.MatrixWrapper
import com.cobblemon.mod.common.client.trade.ClientTradeOffer
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.getPlayer
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.world.phys.Vec3
import java.util.*

class ClientPlayerActionRequests {
    val battleChallenges = mutableListOf<ClientBattleChallenge>()
    val multiBattleTeamRequests = mutableListOf<ClientBattleChallenge>()
    val tradeOffers = mutableListOf<ClientTradeOffer>()

    private val particleDict = mutableMapOf<UUID, ParticleStorm>()
    fun addParticleEffect(challenge: ClientBattleChallenge) {
        challenge.challengerIds.forEach {
            val player = it.getPlayer()
            if (player != null) {
                val wrapper = MatrixWrapper()
                val matrix = PoseStack()
                matrix.translate(player.x, player.eyeY + 1.1, player.z)
                wrapper.updateMatrix(matrix.last().pose())
                val effect = BedrockParticleOptionsRepository.getEffect(cobblemonResource("particle_exclamation"))
                if (effect != null) {
                    val particleStorm = ParticleStorm(
                            effect = effect,
                            matrixWrapper = wrapper,
                            world = Minecraft.getInstance().level ?: return,
                            entity = player,
                    )
                    particleStorm.spawn()
                    particleDict[it] = particleStorm
                }
            }
        }
    }


    fun onTick() {

        particleDict.forEach {
            val player = it.key.getPlayer()
            if (player != null) {
                val matrix = PoseStack()
                matrix.translate(player.x, player.eyeY + 1.1, player.z)
                it.value.matrixWrapper.updateMatrix(matrix.last().pose())
            } else {
                it.value.remove()
                particleDict.remove(it.key)
            }
        }
    }
}