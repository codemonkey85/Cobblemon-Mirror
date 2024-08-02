/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.snowstorm

import com.bedrockk.molang.Expression
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
import net.minecraft.world.phys.Vec3

/**
 * A type of view direction for rotating a particle's rendering.
 *
 * @author Hiroku
 * @since May 14th, 2023
 */
interface ParticleViewDirection : CodecMapped {
    companion object : ArbitrarilyMappedSerializableCompanion<ParticleViewDirection, ParticleViewDirectionType>(
        keyFromValue = { it.type },
        keyFromString = ParticleViewDirectionType::valueOf,
        stringFromKey = { it.name }
    ) {
        init {
            registerSubtype(ParticleViewDirectionType.CUSTOM, CustomViewDirection::class.java, CustomViewDirection.CODEC)
            registerSubtype(ParticleViewDirectionType.FROM_MOTION, FromMotionViewDirection::class.java, FromMotionViewDirection.CODEC)
        }
    }

    val type: ParticleViewDirectionType
    fun getDirection(runtime: MoLangRuntime, lastDirection: Vec3, currentVelocity: Vec3): Vec3
}

class FromMotionViewDirection(var minSpeed: Double = 0.01) : ParticleViewDirection {
    companion object {
        val CODEC: Codec<FromMotionViewDirection> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name },
                PrimitiveCodec.DOUBLE.fieldOf("minSpeed").forGetter { it.minSpeed }
            ).apply(instance) { _, minSpeed -> FromMotionViewDirection(minSpeed) }
        }
    }

    override val type: ParticleViewDirectionType = ParticleViewDirectionType.FROM_MOTION
    override fun <T> encode(ops: DynamicOps<T>) = CODEC.encodeStart(ops, this)
    override fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeDouble(minSpeed)
    }

    override fun readFromBuffer(buffer: RegistryFriendlyByteBuf) {
        minSpeed = buffer.readDouble()
    }

    override fun getDirection(runtime: MoLangRuntime, lastDirection: Vec3, currentVelocity: Vec3): Vec3 {
        return if (currentVelocity.length() * 20 >= minSpeed) {
            currentVelocity.normalize()
        } else {
            lastDirection
        }
    }
}

class CustomViewDirection(var direction: Triple<Expression, Expression, Expression>) : ParticleViewDirection {
    companion object {
        val CODEC: Codec<CustomViewDirection> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name },
                EXPRESSION_CODEC.fieldOf("directionX").forGetter { it.direction.first },
                EXPRESSION_CODEC.fieldOf("directionY").forGetter { it.direction.second },
                EXPRESSION_CODEC.fieldOf("directionZ").forGetter { it.direction.third },
            ).apply(instance) { _, directionX, directionY, directionZ -> CustomViewDirection(Triple(directionX, directionY, directionZ)) }
        }
    }

    override val type = ParticleViewDirectionType.CUSTOM

    override fun <T> encode(ops: DynamicOps<T>) = CODEC.encodeStart(ops, this)
    override fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(direction.first.getString())
        buffer.writeString(direction.second.getString())
        buffer.writeString(direction.third.getString())
    }

    override fun readFromBuffer(buffer: RegistryFriendlyByteBuf) {
        direction = Triple(
            buffer.readString().asExpression(),
            buffer.readString().asExpression(),
            buffer.readString().asExpression()
        )
    }

    override fun getDirection(runtime: MoLangRuntime, lastDirection: Vec3, currentVelocity: Vec3) = runtime.resolveVec3d(direction)
}

enum class ParticleViewDirectionType {
    CUSTOM,
    FROM_MOTION
}
