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
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf

abstract class ParticleUVMode : CodecMapped {
    companion object : ArbitrarilyMappedSerializableCompanion<ParticleUVMode, ParticleUVModeType>(
        keyFromValue = { it.type },
        stringFromKey = { it.name },
        keyFromString = ParticleUVModeType::valueOf
    ) {
        init {
            registerSubtype(ParticleUVModeType.ANIMATED, AnimatedParticleUVMode::class.java, AnimatedParticleUVMode.CODEC)
            registerSubtype(ParticleUVModeType.STATIC, StaticParticleUVMode::class.java, StaticParticleUVMode.CODEC)
        }
    }

    abstract val type: ParticleUVModeType
    open var startU: Expression = NumberExpression(0.0)
    open var startV: Expression = NumberExpression(0.0)
    open var textureSizeX: Int = 8
    open var textureSizeY: Int = 8
    open var uSize: Expression = NumberExpression(8.0)
    open var vSize: Expression = NumberExpression(8.0)

    abstract fun get(moLangRuntime: MoLangRuntime, age: Double, maxAge: Double, uvDetails: UVDetails): UVDetails
}

enum class ParticleUVModeType {
    STATIC,
    ANIMATED
}

class AnimatedParticleUVMode(
    override var startU: Expression = NumberExpression(0.0),
    override var startV: Expression = NumberExpression(0.0),
    override var textureSizeX: Int = 8,
    override var textureSizeY: Int = 8,
    override var uSize: Expression = NumberExpression(8.0),
    override var vSize: Expression = NumberExpression(8.0),
    var stepU: Expression = NumberExpression(8.0),
    var stepV: Expression = NumberExpression(0.0),
    var maxFrame: Expression = NumberExpression(0.0),
    var fps: Expression = NumberExpression(1.0),
    var stretchToLifetime: Boolean = false,
    var loop: Boolean = false
) : ParticleUVMode() {
    override val type = ParticleUVModeType.ANIMATED

    companion object {
        val CODEC: Codec<AnimatedParticleUVMode> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name },
                EXPRESSION_CODEC.fieldOf("startU").forGetter { it.startU },
                EXPRESSION_CODEC.fieldOf("startV").forGetter { it.startV },
                PrimitiveCodec.INT.fieldOf("textureSizeX").forGetter { it.textureSizeX },
                PrimitiveCodec.INT.fieldOf("textureSizeY").forGetter { it.textureSizeY },
                EXPRESSION_CODEC.fieldOf("uSize").forGetter { it.uSize },
                EXPRESSION_CODEC.fieldOf("vSize").forGetter { it.vSize },
                EXPRESSION_CODEC.fieldOf("stepU").forGetter { it.stepU },
                EXPRESSION_CODEC.fieldOf("stepV").forGetter { it.stepV },
                EXPRESSION_CODEC.fieldOf("maxFrame").forGetter { it.maxFrame },
                EXPRESSION_CODEC.fieldOf("fps").forGetter { it.fps },
                PrimitiveCodec.BOOL.fieldOf("stretchToLifetime").forGetter { it.stretchToLifetime },
                PrimitiveCodec.BOOL.fieldOf("loop").forGetter { it.loop }
            ).apply(instance) { _, startU, startV, textureSizeX, textureSizeY, uSize, vSize, stepU, stepV, maxFrame, fps, stretchToLifetime, loop ->
                AnimatedParticleUVMode(startU, startV, textureSizeX, textureSizeY, uSize, vSize, stepU, stepV, maxFrame, fps, stretchToLifetime, loop)
            }
        }
    }

    override fun <T> encode(ops: DynamicOps<T>): DataResult<T> {
        return CODEC.encodeStart(ops, this)
    }

    override fun readFromBuffer(buffer: RegistryFriendlyByteBuf) {
        startU = MoLang.createParser(buffer.readString()).parseExpression()
        startV = MoLang.createParser(buffer.readString()).parseExpression()
        textureSizeX = buffer.readInt()
        textureSizeY = buffer.readInt()
        uSize = MoLang.createParser(buffer.readString()).parseExpression()
        vSize = MoLang.createParser(buffer.readString()).parseExpression()
        stepU = MoLang.createParser(buffer.readString()).parseExpression()
        stepV = MoLang.createParser(buffer.readString()).parseExpression()
        maxFrame = MoLang.createParser(buffer.readString()).parseExpression()
        fps = MoLang.createParser(buffer.readString()).parseExpression()
        stretchToLifetime = buffer.readBoolean()
        loop = buffer.readBoolean()
    }

    override fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(startU.getString())
        buffer.writeString(startV.getString())
        buffer.writeInt(textureSizeX)
        buffer.writeInt(textureSizeY)
        buffer.writeString(uSize.getString())
        buffer.writeString(vSize.getString())
        buffer.writeString(stepU.getString())
        buffer.writeString(stepV.getString())
        buffer.writeString(maxFrame.getString())
        buffer.writeString(fps.getString())
        buffer.writeBoolean(stretchToLifetime)
        buffer.writeBoolean(loop)
    }

    override fun get(runtime: MoLangRuntime, age: Double, maxAge: Double, uvDetails: UVDetails): UVDetails {
        val maxFrame = runtime.resolveInt(maxFrame) - 1
        val stepU = runtime.resolveDouble(stepU)
        val stepV = runtime.resolveDouble(stepV)
        val uSize = runtime.resolveDouble(uSize)
        val vSize = runtime.resolveDouble(vSize)


        if (stretchToLifetime) {
            val frame = ((age / maxAge) * maxFrame).toInt()
            val startU = runtime.resolveDouble(startU) + frame * stepU
            val startV = runtime.resolveDouble(startV) + frame * stepV
            return uvDetails.set(
                startU = startU / textureSizeX,
                startV = startV / textureSizeY,
                endU = (startU + uSize) / textureSizeX,
                endV = (startV + vSize) / textureSizeY
            )
        } else {
            val fps = runtime.resolveDouble(fps)
            val effectiveFrame = ((age * fps) % maxFrame).toInt()
            val frame = if (!loop && age * fps >= maxFrame) {
                maxFrame
            } else {
                effectiveFrame
            }

            val startU = runtime.resolveDouble(startU) + frame * stepU
            val startV = runtime.resolveDouble(startV) + frame * stepV

            return uvDetails.set(
                startU = startU / textureSizeX,
                startV = startV / textureSizeY,
                endU = (startU + uSize) / textureSizeX,
                endV = (startV + vSize) / textureSizeY
            )
        }
    }
}

class StaticParticleUVMode(
    override var startU: Expression = NumberExpression(0.0),
    override var startV: Expression = NumberExpression(0.0),
    override var textureSizeX: Int = 8,
    override var textureSizeY: Int = 8,
    override var uSize: Expression = NumberExpression(8.0),
    override var vSize: Expression = NumberExpression(8.0),
) : ParticleUVMode() {
    override val type = ParticleUVModeType.STATIC

    companion object {
        val CODEC: Codec<StaticParticleUVMode> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name },
                EXPRESSION_CODEC.fieldOf("startU").forGetter { it.startU },
                EXPRESSION_CODEC.fieldOf("startV").forGetter { it.startV },
                PrimitiveCodec.INT.fieldOf("textureSizeX").forGetter { it.textureSizeX },
                PrimitiveCodec.INT.fieldOf("textureSizeY").forGetter { it.textureSizeY },
                EXPRESSION_CODEC.fieldOf("uSize").forGetter { it.uSize },
                EXPRESSION_CODEC.fieldOf("vSize").forGetter { it.vSize },
            ).apply(instance) { _, startU, startV, textureSizeX, textureSizeY, uSize, vSize ->
                StaticParticleUVMode(startU, startV, textureSizeX, textureSizeY, uSize, vSize)
            }
        }
    }

    override fun get(moLangRuntime: MoLangRuntime, age: Double, maxAge: Double, uvDetails: UVDetails): UVDetails {
        return uvDetails.set(
            startU = moLangRuntime.resolveDouble(startU) / textureSizeX,
            startV = moLangRuntime.resolveDouble(startV) / textureSizeY,
            endU = (moLangRuntime.resolveDouble(startU) + moLangRuntime.resolveDouble(uSize)) / textureSizeX,
            endV = (moLangRuntime.resolveDouble(startV) + moLangRuntime.resolveDouble(vSize)) / textureSizeY
        )
    }

    override fun <T> encode(ops: DynamicOps<T>): DataResult<T> {
        return CODEC.encodeStart(ops, this)
    }

    override fun readFromBuffer(buffer: RegistryFriendlyByteBuf) {
        startU = MoLang.createParser(buffer.readString()).parseExpression()
        startV = MoLang.createParser(buffer.readString()).parseExpression()
        textureSizeX = buffer.readInt()
        textureSizeY = buffer.readInt()
        uSize = MoLang.createParser(buffer.readString()).parseExpression()
        vSize = MoLang.createParser(buffer.readString()).parseExpression()
    }
    override fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(startU.getString())
        buffer.writeString(startV.getString())
        buffer.writeInt(textureSizeX)
        buffer.writeInt(textureSizeY)
        buffer.writeString(uSize.getString())
        buffer.writeString(vSize.getString())
    }
}

class UVDetails {
    var startU: Float = 0F
    var startV: Float = 0F
    var endU: Float = 0F
    var endV: Float = 0F

    fun set(startU: Double, startV: Double, endU: Double, endV: Double): UVDetails {
        this.startU = startU.toFloat()
        this.startV = startV.toFloat()
        this.endU = endU.toFloat()
        this.endV = endV.toFloat()
        return this
    }
}