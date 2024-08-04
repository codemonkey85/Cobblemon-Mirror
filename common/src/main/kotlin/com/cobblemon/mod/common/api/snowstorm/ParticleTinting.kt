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
import com.cobblemon.mod.common.util.codec.EXPRESSION_CODEC
import com.cobblemon.mod.common.util.getString
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.resolveDouble
import com.cobblemon.mod.common.util.writeString
import com.mojang.serialization.Codec
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.util.Mth
import org.joml.Vector4f
import kotlin.math.abs

interface ParticleTinting : CodecMapped {
    companion object : ArbitrarilyMappedSerializableCompanion<ParticleTinting, ParticleTintingType>(
        keyFromString = ParticleTintingType::valueOf,
        stringFromKey = { it.name },
        keyFromValue = { it.type }
    ) {
        init {
            registerSubtype(ParticleTintingType.EXPRESSION, ExpressionParticleTinting::class.java, ExpressionParticleTinting.CODEC)
            registerSubtype(ParticleTintingType.GRADIENT, GradientParticleTinting::class.java, GradientParticleTinting.CODEC)
        }
    }

    val type: ParticleTintingType

    fun getTint(runtime: MoLangRuntime): Vector4f
}

enum class ParticleTintingType {
    EXPRESSION,
    GRADIENT
}

class ExpressionParticleTinting(
    var red: Expression = NumberExpression(1.0),
    var green: Expression = NumberExpression(1.0),
    var blue: Expression = NumberExpression(1.0),
    var alpha: Expression = NumberExpression(1.0)
) : ParticleTinting {
    companion object {
        val CODEC: Codec<ExpressionParticleTinting> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name },
                EXPRESSION_CODEC.fieldOf("red").forGetter { it.red },
                EXPRESSION_CODEC.fieldOf("green").forGetter { it.green },
                EXPRESSION_CODEC.fieldOf("blue").forGetter { it.blue },
                EXPRESSION_CODEC.fieldOf("alpha").forGetter { it.alpha }
            ).apply(instance) { _, red, green, blue, alpha -> ExpressionParticleTinting(red, green, blue, alpha) }
        }
    }

    override val type = ParticleTintingType.EXPRESSION
    override fun getTint(runtime: MoLangRuntime) = Vector4f(
        runtime.resolveDouble(red).toFloat(),
        runtime.resolveDouble(green).toFloat(),
        runtime.resolveDouble(blue).toFloat(),
        runtime.resolveDouble(alpha).toFloat()
    )
    override fun <T> encode(ops: DynamicOps<T>) = CODEC.encodeStart(ops, this)

    override fun readFromBuffer(buffer: RegistryFriendlyByteBuf) {
        red = MoLang.createParser(buffer.readString()).parseExpression()
        green = MoLang.createParser(buffer.readString()).parseExpression()
        blue = MoLang.createParser(buffer.readString()).parseExpression()
        alpha = MoLang.createParser(buffer.readString()).parseExpression()
    }

    override fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(red.getString())
        buffer.writeString(green.getString())
        buffer.writeString(blue.getString())
        buffer.writeString(alpha.getString())
    }
}

class GradientParticleTinting(
    var interpolant: Expression = NumberExpression(0.0),
    var gradient: Map<Double, Vector4f> = emptyMap()
) : ParticleTinting {
    class GradientEntry(val key: Double, val colour: Vector4f) {
        companion object {
            val CODEC: Codec<GradientEntry> = RecordCodecBuilder.create { instance ->
                instance.group(
                    PrimitiveCodec.DOUBLE.fieldOf("key").forGetter { it.key },
                    PrimitiveCodec.FLOAT.fieldOf("red").forGetter { it.colour.x },
                    PrimitiveCodec.FLOAT.fieldOf("green").forGetter { it.colour.y },
                    PrimitiveCodec.FLOAT.fieldOf("blue").forGetter { it.colour.z },
                    PrimitiveCodec.FLOAT.fieldOf("alpha").forGetter { it.colour.w }
                ).apply(instance) { key, red, green, blue, alpha -> GradientEntry(key, Vector4f(red, green, blue, alpha)) }
            }
        }

        fun toEntry() = key to colour
    }

    companion object {
        val CODEC: Codec<GradientParticleTinting> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name },
                EXPRESSION_CODEC.fieldOf("interpolant").forGetter { it.interpolant },
                GradientEntry.CODEC.listOf().fieldOf("gradient").forGetter { it.gradient.entries.map { (key, colour) -> GradientEntry(key, colour) } }
            ).apply(instance) { _, interpolant, gradient ->
                GradientParticleTinting(
                    interpolant = interpolant,
                    gradient = mapOf(*gradient.map { it.toEntry() }.toTypedArray())
                )
            }
        }
    }

    override val type = ParticleTintingType.GRADIENT
    override fun getTint(runtime: MoLangRuntime): Vector4f {
        val interpolant = runtime.resolveDouble(interpolant)
        val closestBelowNode = gradient.entries
            .filter { it.key <= interpolant }
            .minByOrNull { abs(it.key - interpolant) }
        val closestAboveNode = gradient.entries
            .filter { it.key >= interpolant }
            .minByOrNull { abs(it.key - interpolant) }

        if (closestBelowNode == null && closestAboveNode == null) {
            throw IllegalStateException("A gradient particle tinting had no below node and no above node, which is probably only possible if the gradient has no points.")
        }

        if (closestBelowNode == null) {
            return closestAboveNode!!.value
        } else if (closestAboveNode == null) {
            return closestBelowNode.value
        } else {
            val progression = ((interpolant - closestBelowNode.key) / (closestAboveNode.key - closestBelowNode.key)).toFloat()
            return Vector4f(
                Mth.lerp(progression, closestBelowNode.value.x, closestAboveNode.value.x),
                Mth.lerp(progression, closestBelowNode.value.y, closestAboveNode.value.y),
                Mth.lerp(progression, closestBelowNode.value.z, closestAboveNode.value.z),
                Mth.lerp(progression, closestBelowNode.value.w, closestAboveNode.value.w)
            )
        }

    }

    override fun <T> encode(ops: DynamicOps<T>) = CODEC.encodeStart(ops, this)

    override fun readFromBuffer(buffer: RegistryFriendlyByteBuf) {
        interpolant = MoLang.createParser(buffer.readString()).parseExpression()
        gradient = buffer
            .readList { buffer.readDouble() to Vector4f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat()) }
            .toMap()
    }

    override fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(interpolant.getString())
        buffer.writeCollection(gradient.entries) { pb, (key, colour) ->
            buffer.writeDouble(key)
            buffer.writeFloat(colour.x)
            buffer.writeFloat(colour.y)
            buffer.writeFloat(colour.z)
            buffer.writeFloat(colour.w)
        }
    }
}