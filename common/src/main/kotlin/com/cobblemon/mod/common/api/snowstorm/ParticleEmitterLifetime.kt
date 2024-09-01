/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.snowstorm

import com.bedrockk.molang.Expression
import com.bedrockk.molang.MoLang
import com.bedrockk.molang.ast.NumberExpression
import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.api.codec.CodecMapped
import com.cobblemon.mod.common.api.data.ArbitrarilyMappedSerializableCompanion
import com.cobblemon.mod.common.util.*
import com.cobblemon.mod.common.util.codec.EXPRESSION_CODEC
import com.mojang.serialization.Codec
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf

interface ParticleEmitterLifetime : CodecMapped {
    companion object : ArbitrarilyMappedSerializableCompanion<ParticleEmitterLifetime, ParticleEmitterLifetimeType>(
        keyFromValue = { it.type },
        keyFromString = ParticleEmitterLifetimeType::valueOf,
        stringFromKey = { it.name }
    ) {
        init {
            registerSubtype(ParticleEmitterLifetimeType.ONCE, OnceEmitterLifetime::class.java, OnceEmitterLifetime.CODEC)
            registerSubtype(ParticleEmitterLifetimeType.EXPRESSION, ExpressionEmitterLifetime::class.java, ExpressionEmitterLifetime.CODEC)
            registerSubtype(ParticleEmitterLifetimeType.LOOPING, LoopingEmitterLifetime::class.java, LoopingEmitterLifetime.CODEC)
        }
    }

    val type: ParticleEmitterLifetimeType
    fun getAction(runtime: MoLangRuntime, started: Boolean, emitterAge: Double): ParticleEmitterAction
}

class OnceEmitterLifetime(var activeTime: Expression = 1.0.asExpression()) : ParticleEmitterLifetime {
    companion object {
        val CODEC: Codec<OnceEmitterLifetime> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name },
                EXPRESSION_CODEC.fieldOf("activeTime").forGetter { it.activeTime }
            ).apply(instance) { _, activeTime -> OnceEmitterLifetime(activeTime) }
        }
    }

    override val type = ParticleEmitterLifetimeType.ONCE

    override fun getAction(runtime: MoLangRuntime, started: Boolean, emitterAge: Double): ParticleEmitterAction {
        val activeTime = runtime.resolve(activeTime)
        runtime.environment.setSimpleVariable("emitter_lifetime", activeTime)
        return if (emitterAge > activeTime.asDouble()) {
            ParticleEmitterAction.STOP
        } else {
            ParticleEmitterAction.GO
        }
    }

    override fun <T> encode(ops: DynamicOps<T>) = CODEC.encodeStart(ops, this)
    override fun readFromBuffer(buffer: RegistryFriendlyByteBuf) {
        activeTime = MoLang.createParser(buffer.readString()).parseExpression()
    }

    override fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(activeTime.getString())
    }
}

class ExpressionEmitterLifetime(var activation: Expression = NumberExpression(0.0), var expiration: Expression = NumberExpression(0.0)) : ParticleEmitterLifetime {
    companion object {
        val CODEC: Codec<ExpressionEmitterLifetime> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name },
                EXPRESSION_CODEC.fieldOf("activation").forGetter { it.activation },
                EXPRESSION_CODEC.fieldOf("expiration").forGetter { it.expiration }
            ).apply(instance) { _, activation, expiration -> ExpressionEmitterLifetime(activation, expiration) }
        }
    }

    override val type = ParticleEmitterLifetimeType.EXPRESSION

    override fun getAction(runtime: MoLangRuntime, started: Boolean, emitterAge: Double): ParticleEmitterAction {
        if (started) {
            if (runtime.resolveBoolean(expiration)) {
                return ParticleEmitterAction.STOP
            } else {
                return ParticleEmitterAction.GO
            }
        } else {
            if (runtime.resolveBoolean(activation)) {
                return ParticleEmitterAction.GO
            } else {
                return ParticleEmitterAction.NOTHING
            }
        }
    }

    override fun <T> encode(ops: DynamicOps<T>) = CODEC.encodeStart(ops, this)
    override fun readFromBuffer(buffer: RegistryFriendlyByteBuf) {
        activation = MoLang.createParser(buffer.readString()).parseExpression()
        expiration = MoLang.createParser(buffer.readString()).parseExpression()
    }

    override fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(activation.getString())
        buffer.writeString(expiration.getString())
    }
}

class LoopingEmitterLifetime(var activeTime: Expression = 1.0.asExpression(), var sleepTime: Expression = 1.0.asExpression()) : ParticleEmitterLifetime {
    companion object {
        val CODEC: Codec<LoopingEmitterLifetime> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name },
                EXPRESSION_CODEC.fieldOf("activeTime").forGetter { it.activeTime },
                EXPRESSION_CODEC.fieldOf("sleepTime").forGetter { it.sleepTime }
            ).apply(instance) { _, activeTime, sleepTime -> LoopingEmitterLifetime(activeTime, sleepTime) }
        }
    }

    override val type = ParticleEmitterLifetimeType.LOOPING

    override fun getAction(runtime: MoLangRuntime, started: Boolean, emitterAge: Double): ParticleEmitterAction {
        val activeTime = runtime.resolve(activeTime)
        val activeTimeValue = activeTime.asDouble()
        val sleepTime = runtime.resolveDouble(sleepTime)
        val interval = activeTimeValue + sleepTime
        val displacement = emitterAge % interval
        runtime.environment.setSimpleVariable("emitter_lifetime", activeTime)

        if (emitterAge > activeTimeValue && sleepTime == 0.0) {
            return ParticleEmitterAction.STOP
        }

        return if (displacement < activeTimeValue) {
            ParticleEmitterAction.GO
        } else {
            ParticleEmitterAction.RESET
        }
    }

    override fun <T> encode(ops: DynamicOps<T>) = CODEC.encodeStart(ops, this)
    override fun readFromBuffer(buffer: RegistryFriendlyByteBuf) {
        activeTime = MoLang.createParser(buffer.readString()).parseExpression()
        sleepTime = MoLang.createParser(buffer.readString()).parseExpression()
    }

    override fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(activeTime.getString())
        buffer.writeString(sleepTime.getString())
    }
}

enum class ParticleEmitterLifetimeType {
    LOOPING,
    ONCE,
    EXPRESSION
}

enum class ParticleEmitterAction {
    NOTHING,
    GO,
    STOP,
    RESET
}