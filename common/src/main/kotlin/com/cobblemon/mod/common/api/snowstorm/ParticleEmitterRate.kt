/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.snowstorm

import com.bedrockk.molang.Expression
import com.bedrockk.molang.ast.NumberExpression
import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.api.codec.CodecMapped
import com.cobblemon.mod.common.api.data.ArbitrarilyMappedSerializableCompanion
import com.cobblemon.mod.common.api.snowstorm.ParticleEmitterRate.Companion.OVERFLOW_VARIABLE
import com.cobblemon.mod.common.util.*
import com.cobblemon.mod.common.util.codec.EXPRESSION_CODEC
import com.mojang.serialization.Codec
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import java.lang.Integer.min

interface ParticleEmitterRate : CodecMapped {
    companion object : ArbitrarilyMappedSerializableCompanion<ParticleEmitterRate, ParticleEmitterRateType>(
        keyFromValue = { it.type },
        keyFromString = ParticleEmitterRateType::valueOf,
        stringFromKey = { it.name }
    ) {
        const val OVERFLOW_VARIABLE = "emitter_overflow"
        init {
            registerSubtype(ParticleEmitterRateType.INSTANT, InstantParticleEmitterRate::class.java, InstantParticleEmitterRate.CODEC)
            registerSubtype(ParticleEmitterRateType.STEADY, SteadyParticleEmitterRate::class.java, SteadyParticleEmitterRate.CODEC)
        }
    }

    val type: ParticleEmitterRateType
    fun getEmitCount(runtime: MoLangRuntime, started: Boolean, currentlyActive: Int): Int
}

enum class ParticleEmitterRateType {
    STEADY,
    INSTANT
}

class InstantParticleEmitterRate(var amount: Expression = "1".asExpression()) : ParticleEmitterRate {
    companion object {
        val CODEC: Codec<InstantParticleEmitterRate> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name },
                EXPRESSION_CODEC.fieldOf("amount").forGetter { it.amount }
            ).apply(instance) { _, amount -> InstantParticleEmitterRate(amount) }
        }
    }

    override val type = ParticleEmitterRateType.INSTANT

    override fun getEmitCount(runtime: MoLangRuntime, started: Boolean, currentlyActive: Int): Int {
        if (started) {
            return 0
        } else {
            return runtime.resolveInt(amount)
        }
    }

    override fun <T> encode(ops: DynamicOps<T>) = CODEC.encodeStart(ops, this)
    override fun readFromBuffer(buffer: RegistryFriendlyByteBuf) {
        amount = buffer.readString().asExpression()
    }

    override fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(amount.getString())
    }
}

class SteadyParticleEmitterRate(
    var rate: Expression = NumberExpression(0.0),
    var maximum: Expression = NumberExpression(0.0)
) : ParticleEmitterRate {

    var time = System.currentTimeMillis()

    companion object {
        val CODEC: Codec<SteadyParticleEmitterRate> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name },
                EXPRESSION_CODEC.fieldOf("rate").forGetter { it.rate },
                EXPRESSION_CODEC.fieldOf("maximum").forGetter { it.maximum }
            ).apply(instance) { _, rate, maximum -> SteadyParticleEmitterRate(rate, maximum) }
        }
    }

    override val type = ParticleEmitterRateType.STEADY

    override fun getEmitCount(runtime: MoLangRuntime, started: Boolean, currentlyActive: Int): Int {
        // The emitting rates are all per second, but this runs every tick. Presents some difficulties.

        val max = runtime.resolveDouble(maximum).toInt()
        val variables = runtime.environment.variable

        /*
         * The strategy is that per tick it might be calculated to be 1.2 particles. We can't spawn a fifth
         * of a particle, so we consider the .2 to be overflow which will be added to the next total. If the
         * next tick we end up with 1.9, the overflow from last time will bring it to 2.1, so 2 particles with
         * 0.1 overflow for the next tick.
         */

        val currentOverflow = variables.map[OVERFLOW_VARIABLE]?.asDouble() ?: 0.0
        if (currentlyActive >= max) {
            return 0
        }

        val perSecond = runtime.resolveDouble(rate)

        val trySpawn = perSecond / 20.0 + currentOverflow

        val intComponent = trySpawn.toInt() // round down
        val doubleComponent = trySpawn - intComponent // decimal portion (the overflow)

        variables.map[OVERFLOW_VARIABLE] = DoubleValue(doubleComponent)
        return min(intComponent, max - currentlyActive)
    }

    override fun <T> encode(ops: DynamicOps<T>) = CODEC.encodeStart(ops, this)
    override fun readFromBuffer(buffer: RegistryFriendlyByteBuf) {
        rate = buffer.readString().asExpression()
        maximum = buffer.readString().asExpression()
    }

    override fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(rate.getString())
        buffer.writeString(maximum.getString())
    }
}