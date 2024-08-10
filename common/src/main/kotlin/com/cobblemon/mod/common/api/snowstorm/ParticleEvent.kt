/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.snowstorm

import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.molang.MoLangFunctions.getQueryStruct
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.net.Decodable
import com.cobblemon.mod.common.api.net.Encodable
import com.cobblemon.mod.common.client.particle.BedrockParticleEffectRepository
import com.cobblemon.mod.common.client.particle.ParticleStorm
import com.cobblemon.mod.common.client.render.MatrixWrapper
import com.cobblemon.mod.common.client.render.SnowstormParticle
import com.cobblemon.mod.common.util.asExpressionLike
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.PacketByteBuf
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d

/**
 * An event that can be referenced from various particle event triggers. The events are not necessarily particles,
 * but can be combinations of particles, sounds, and MoLang expressions.
 *
 * @author Hiroku
 * @since March 2nd, 2024
 */
class ParticleEvent(
    var particleEffect: EventParticleEffect? = null,
    var soundEffect: EventSoundEffect? = null,
    var expression: ExpressionLike? = null
): Encodable, Decodable {
    companion object {
        val CODEC: Codec<ParticleEvent> = RecordCodecBuilder.create { instance ->
            instance.group(
                EventParticleEffect.CODEC.optionalFieldOf("particle_effect", null).forGetter { it.particleEffect },
                EventSoundEffect.CODEC.optionalFieldOf("sound_effect", null).forGetter { it.soundEffect },
                PrimitiveCodec.STRING.optionalFieldOf("expression", null).forGetter { it.expression?.toString() }
            ).apply(instance) { particleEffect, soundEffect, expression ->
                ParticleEvent(
                    particleEffect,
                    soundEffect,
                    expression?.asExpressionLike()
                )
            }
        }
    }

    override fun encode(buffer: PacketByteBuf) {
        buffer.writeNullable(particleEffect) { pb, effect ->
            pb.writeIdentifier(effect.effect)
            pb.writeEnumConstant(effect.type)
            pb.writeNullable(effect.expression) { pb, expr -> pb.writeString(expr.toString()) }
        }
        buffer.writeNullable(soundEffect) { pb, effect -> pb.writeIdentifier(effect.sound) }
        buffer.writeNullable(expression) { pb, expr -> pb.writeString(expr.toString()) }
    }
    override fun decode(buffer: PacketByteBuf) {
        particleEffect = buffer.readNullable { pb -> EventParticleEffect(
            pb.readIdentifier(),
            pb.readEnumConstant(EventParticleEffect.EventParticleType::class.java),
            pb.readNullable { pb.readString().asExpressionLike() }
        ) }
        soundEffect = buffer.readNullable { pb -> EventSoundEffect(pb.readIdentifier()) }
        expression = buffer.readNullable { pb -> pb.readString().asExpressionLike() }
    }

    fun run(storm: ParticleStorm, particle: SnowstormParticle?) {
        particleEffect?.let { effect ->
            val bedrockParticleEffect = BedrockParticleEffectRepository.getEffect(effect.effect) ?: return@let
            val rootMatrix = when (effect.type) {
                EventParticleEffect.EventParticleType.EMITTER,// -> MatrixWrapper().updatePosition(storm.matrixWrapper.getOrigin())
                EventParticleEffect.EventParticleType.EMITTER_BOUND,// -> storm.matrixWrapper
                EventParticleEffect.EventParticleType.PARTICLE,
                EventParticleEffect.EventParticleType.PARTICLE_WITH_VELOCITY -> (particle?.let { Vec3d(it.getX(), it.getY(), it.getZ()) } ?: Vec3d(storm.getX(), storm.getY(), storm.getZ())).let { MatrixWrapper().updatePosition(it) }
            }

            val sourceVelocity = when (effect.type) {
                EventParticleEffect.EventParticleType.EMITTER,// -> storm.sourceVelocity().let { { it } }
                EventParticleEffect.EventParticleType.EMITTER_BOUND,// -> storm.sourceVelocity
                EventParticleEffect.EventParticleType.PARTICLE -> { { Vec3d.ZERO } }
                EventParticleEffect.EventParticleType.PARTICLE_WITH_VELOCITY -> (particle?.let { Vec3d(it.getVelocityX(), it.getVelocityY(), it.getVelocityZ()) } ?: Vec3d.ZERO).let { { it } }
            }

            val newStorm = ParticleStorm(
                effect = bedrockParticleEffect,
                matrixWrapper = rootMatrix,
                world = storm.world,
                sourceVelocity = sourceVelocity,
                sourceAlive = storm.sourceAlive,
                sourceVisible = storm.sourceVisible,
                onDespawn = {},
                runtime = MoLangRuntime().setup().also { it.environment.structs["query"] = storm.runtime.environment.getQueryStruct() },
                entity = storm.entity
            )

            effect.expression?.resolve(newStorm.runtime)

            newStorm.spawn()
        }
        soundEffect?.let { effect ->
            val position = particle?.let { Vec3d(it.getX(), it.getY(), it.getZ()) } ?: Vec3d(storm.getX(), storm.getY(), storm.getZ())
            val world = storm.world
            val soundEvent = SoundEvent.of(effect.sound)
            world.playSound(position.x, position.y, position.z, soundEvent, SoundCategory.NEUTRAL, 1F, 1F, true)
        }
        expression?.resolve(storm.runtime)
    }
}

/**
 * A particle component of a [ParticleEvent]. It contains the effect to play, the spawning type, and a pre-run
 * expression to run in the new storm's runtime.
 *
 * @author Hiroku
 * @since March 2nd, 2024
 */
class EventParticleEffect(
    val effect: Identifier,
    val type: EventParticleType,
    val expression: ExpressionLike? = null
) {
    companion object {
        val CODEC = RecordCodecBuilder.create<EventParticleEffect> { instance ->
            instance.group(
                Identifier.CODEC.fieldOf("effect").forGetter { it.effect },
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name },
                PrimitiveCodec.STRING.optionalFieldOf("expression", null).forGetter { it.expression?.toString() }
            ).apply(instance) { effect, type, expression ->
                EventParticleEffect(
                    effect,
                    EventParticleType.valueOf(type),
                    expression?.asExpressionLike()
                )
            }
        }
    }

    enum class EventParticleType {
        EMITTER,
        EMITTER_BOUND,
        PARTICLE,
        PARTICLE_WITH_VELOCITY
    }
}

/**
 * A simple sound effect component of a [ParticleEvent]. Plays a sound at the particle location, or if run from an
 * emitter event, at the emitter's location.
 *
 * @author Hiroku
 * @since March 2nd, 2024
 */
class EventSoundEffect(
    val sound: Identifier,
) {
    companion object {
        val CODEC = RecordCodecBuilder.create<EventSoundEffect> { instance ->
            instance.group(
                Identifier.CODEC.fieldOf("sound").forGetter { it.sound }
            ).apply(instance, ::EventSoundEffect)
        }
    }
}