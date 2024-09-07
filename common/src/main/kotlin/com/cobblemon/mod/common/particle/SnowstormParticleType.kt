/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.particle

import com.cobblemon.mod.common.api.snowstorm.BedrockParticleOptions
import com.cobblemon.mod.common.client.particle.ParticleStorm
import com.cobblemon.mod.common.client.render.SnowstormParticle
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.particle.SpriteSet
import net.minecraft.core.particles.ParticleType
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.phys.Vec3

class SnowstormParticleType : ParticleType<SnowstormParticleOptions>(true) {
    companion object {
        val CODEC: MapCodec<SnowstormParticleOptions> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                BedrockParticleOptions.CODEC.fieldOf("effect").forGetter { it.effect }
            ).apply(instance, ::SnowstormParticleOptions)
        }

        val encoder = { effect: SnowstormParticleOptions, buf: RegistryFriendlyByteBuf ->
            effect.effect.writeToBuffer(buf)
        }

        val decoder = { buf: RegistryFriendlyByteBuf ->
            SnowstormParticleOptions(BedrockParticleOptions().also { it.readFromBuffer(buf) })
        }

        val PACKET_CODEC = StreamCodec.ofMember(encoder, decoder)

    }

    class Factory(val spriteProvider: SpriteSet) : ParticleProvider<SnowstormParticleOptions> {
        override fun createParticle(
            parameters: SnowstormParticleOptions,
            world: ClientLevel,
            x: Double,
            y: Double,
            z: Double,
            velocityX: Double,
            velocityY: Double,
            velocityZ: Double
        ): Particle {
            return SnowstormParticle(
                ParticleStorm.contextStorm!!,
                world,
                x, y, z,
                Vec3(velocityX, velocityY, velocityZ),
                invisible = false
            )
        }
    }

    override fun codec() = CODEC

    override fun streamCodec(): StreamCodec<in RegistryFriendlyByteBuf, SnowstormParticleOptions> {
        TODO("Not yet implemented")
    }
}