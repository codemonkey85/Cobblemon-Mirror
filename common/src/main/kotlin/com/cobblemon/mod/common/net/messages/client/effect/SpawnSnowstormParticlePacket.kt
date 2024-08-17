/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.effect

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.snowstorm.BedrockParticleOptions
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.writeIdentifier
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3

/**
 * A packet sent to the client to spawn a [BedrockParticleOptions] at the specified coordinates and rotation.
 *
 * Handled by [com.cobblemon.mod.common.client.net.effect.SpawnSnowstormParticleHandler].
 *
 * @author Hiroku
 * @since January 21st, 2022
 */
class SpawnSnowstormParticlePacket(
    val effectId: ResourceLocation,
    val position: Vec3,
) : NetworkPacket<SpawnSnowstormParticlePacket> {
    override val id = ID
    companion object {
        val ID = cobblemonResource("spawn_snowstorm_particle")
        fun decode(buffer: RegistryFriendlyByteBuf): SpawnSnowstormParticlePacket {
            return SpawnSnowstormParticlePacket(
                effectId = buffer.readIdentifier(),
                position = Vec3(
                    buffer.readDouble(),
                    buffer.readDouble(),
                    buffer.readDouble()
                )
            )
        }
    }
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeIdentifier(effectId)
        buffer.writeDouble(position.x)
        buffer.writeDouble(position.y)
        buffer.writeDouble(position.z)
    }
}