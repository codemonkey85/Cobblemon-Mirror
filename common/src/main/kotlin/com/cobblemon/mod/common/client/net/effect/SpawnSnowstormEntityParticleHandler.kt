/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.effect

import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.ClientMoLangFunctions.setupClient
import com.cobblemon.mod.common.client.particle.BedrockParticleOptionsRepository
import com.cobblemon.mod.common.client.particle.ParticleStorm
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormEntityParticlePacket
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity
import kotlin.math.pow

object SpawnSnowstormEntityParticleHandler : ClientNetworkPacketHandler<SpawnSnowstormEntityParticlePacket> {
    override fun handle(packet: SpawnSnowstormEntityParticlePacket, client: Minecraft) {
        val world = Minecraft.getInstance().level ?: return
        val effect = BedrockParticleOptionsRepository.getEffect(packet.effectId) ?: return
        val entity = world.getEntity(packet.entityId) as? PosableEntity ?: return
        entity as Entity
        val state = entity.delegate as PosableState
        val locator = packet.locator.firstOrNull() { state.locatorStates[it] != null } ?: return
        val matrixWrapper = state.locatorStates[locator]!!

        val particleRuntime = MoLangRuntime().setup().setupClient()
        particleRuntime.environment.query.addFunction("entity") { state.runtime.environment.query }
        particleRuntime.environment.query.addFunction("point_on_curve") {
            //All this just finds a curve that goes through the given points + 0, 0
            //And calcs where we are on it at t
            val t = it.getDouble(0)
            val x1 = it.getDouble(1)
            val y1 = it.getDouble(2)
            val x2 = it.getDouble(3)
            val y2 = it.getDouble(4)

            val a0 = 0.0
            val a1 = x1.pow(2)
            val a2 = x2.pow(2)
            val b0 = 0.0
            val b1 = x1
            val b2 = x2
            val c0 = 1.0
            val c1 = 1.0
            val c2 = 1.0
            val d0 = 0.0
            val d1 = y1
            val d2 = y2

            val matrix = arrayOf(
                arrayOf(a2, b2, c2, d2),
                arrayOf(a1, b1, c1, d1),
                arrayOf(a0, b0, c0, d0)
            )

            for (i in 0 until 3) {
                var pivot = matrix[i][i]
                if (pivot != 0.0) {
                    for (j in 0 until 4) {
                        matrix[i][j] /= pivot
                    }
                }

                // Make other elements in the current column 0
                for (k in 0 until 3) {
                    if (k != i) {
                        val factor = matrix[k][i]
                        for (j in 0 until 4) {
                            matrix[k][j] -= factor * matrix[i][j]
                        }
                    }
                }

            }
            return@addFunction matrix[0][3] * t.pow(2) + matrix[1][3] * t + matrix[2][3]
        }

        val storm = ParticleStorm(
            effect = effect,
            matrixWrapper = matrixWrapper,
            world = world,
            runtime = particleRuntime,
            sourceVelocity = { entity.deltaMovement },
            sourceAlive = { !entity.isRemoved },
            sourceVisible = { !entity.isInvisible },
            entity = entity
        )

        storm.spawn()
    }
}