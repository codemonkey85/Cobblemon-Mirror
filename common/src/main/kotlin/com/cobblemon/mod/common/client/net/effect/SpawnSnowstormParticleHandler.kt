/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.effect

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.particle.BedrockParticleEffectRepository
import com.cobblemon.mod.common.client.particle.ParticleStorm
import com.cobblemon.mod.common.client.render.MatrixWrapper
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormParticlePacket
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack

object SpawnSnowstormParticleHandler : ClientNetworkPacketHandler<SpawnSnowstormParticlePacket> {
    override fun handle(packet: SpawnSnowstormParticlePacket, client: MinecraftClient) {
        val wrapper = MatrixWrapper()
        val matrix = MatrixStack()
        matrix.translate(packet.position.x, packet.position.y, packet.position.z)
//        matrix.multiply(POSITIVE_Y.rotationDegrees(packet.yawDegrees))
//        matrix.multiply(NEGATIVE_X.rotationDegrees(packet.pitchDegrees))
        wrapper.updateMatrix(matrix.peek().positionMatrix)
        val world = MinecraftClient.getInstance().world ?: return
        val effect = BedrockParticleEffectRepository.getEffect(packet.effectId) ?: return
        ParticleStorm(effect, wrapper, world).spawn()
    }
}