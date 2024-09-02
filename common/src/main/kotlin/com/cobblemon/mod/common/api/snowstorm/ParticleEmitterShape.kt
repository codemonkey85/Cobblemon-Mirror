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
import com.cobblemon.mod.common.util.math.convertSphericalToCartesian
import com.cobblemon.mod.common.util.math.getRotationMatrix
import com.cobblemon.mod.common.util.math.times
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.AABB
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import net.minecraft.world.phys.Vec3

interface ParticleEmitterShape : CodecMapped {
    companion object : ArbitrarilyMappedSerializableCompanion<ParticleEmitterShape, ParticleEmitterShapeType>(
        keyFromString = ParticleEmitterShapeType::valueOf,
        stringFromKey = { it.name },
        keyFromValue = { it.type }
    ) {
        init {
            registerSubtype(ParticleEmitterShapeType.SPHERE, SphereParticleEmitterShape::class.java, SphereParticleEmitterShape.CODEC)
            registerSubtype(ParticleEmitterShapeType.POINT, PointParticleEmitterShape::class.java, PointParticleEmitterShape.CODEC)
            registerSubtype(ParticleEmitterShapeType.BOX, BoxParticleEmitterShape::class.java, BoxParticleEmitterShape.CODEC)
            registerSubtype(ParticleEmitterShapeType.DISC, DiscParticleEmitterShape::class.java, DiscParticleEmitterShape.CODEC)
            registerSubtype(ParticleEmitterShapeType.ENTITY_BOUNDING_BOX, EntityBoundingBoxParticleEmitterShape::class.java, EntityBoundingBoxParticleEmitterShape.CODEC)
        }
    }

    val type: ParticleEmitterShapeType

    fun getNewParticlePosition(runtime: MoLangRuntime, entity: Entity?): Vec3
    fun getCenter(runtime: MoLangRuntime, entity: Entity?): Vec3
}

enum class ParticleEmitterShapeType {
    SPHERE,
    POINT,
    BOX,
    DISC,
    ENTITY_BOUNDING_BOX
}

class SphereParticleEmitterShape(
    var offset: Triple<Expression, Expression, Expression> = Triple(
        NumberExpression(0.0),
        NumberExpression(0.0),
        NumberExpression(0.0)
    ),
    var radius: Expression = NumberExpression(0.0),
    var surfaceOnly: Boolean = false
) : ParticleEmitterShape {
    companion object {
        val CODEC: Codec<SphereParticleEmitterShape> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name },
                EXPRESSION_CODEC.fieldOf("offsetX").forGetter { it.offset.first },
                EXPRESSION_CODEC.fieldOf("offsetY").forGetter { it.offset.second },
                EXPRESSION_CODEC.fieldOf("offsetZ").forGetter { it.offset.third },
                EXPRESSION_CODEC.fieldOf("radius").forGetter { it.radius },
                PrimitiveCodec.BOOL.fieldOf("surfaceOnly").forGetter { it.surfaceOnly }
            ).apply(instance) { _, offsetX, offsetY, offsetZ, radius, surfaceOnly ->
                SphereParticleEmitterShape(Triple(offsetX, offsetY, offsetZ), radius, surfaceOnly)
            }
        }
    }

    override val type = ParticleEmitterShapeType.SPHERE

    override fun <T> encode(ops: DynamicOps<T>): DataResult<T> {
        return CODEC.encodeStart(ops, this)
    }

    override fun readFromBuffer(buffer: RegistryFriendlyByteBuf) {
        offset = Triple(
            MoLang.createParser(buffer.readString()).parseExpression(),
            MoLang.createParser(buffer.readString()).parseExpression(),
            MoLang.createParser(buffer.readString()).parseExpression()
        )
        radius = MoLang.createParser(buffer.readString()).parseExpression()
        surfaceOnly = buffer.readBoolean()
    }

    override fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(offset.first.getString())
        buffer.writeString(offset.second.getString())
        buffer.writeString(offset.third.getString())
        buffer.writeString(radius.getString())
        buffer.writeBoolean(surfaceOnly)
    }

    override fun getCenter(runtime: MoLangRuntime, entity: Entity?): Vec3 {
        return runtime.resolveVec3d(offset)
    }

    override fun getNewParticlePosition(runtime: MoLangRuntime, entity: Entity?): Vec3 {
        val radius = runtime.resolveDouble(radius) * if (surfaceOnly) 1.0 else Random.Default.nextDouble()
        val theta = Math.PI * 2 * Random.Default.nextDouble()
        val psi = Math.PI * 2 * Random.Default.nextDouble()
        return getCenter(runtime, entity).add(convertSphericalToCartesian(radius = radius, theta = theta, psi = psi))
    }
}

class PointParticleEmitterShape(
    var offset: Triple<Expression, Expression, Expression> = Triple(
        NumberExpression(0.0),
        NumberExpression(0.0),
        NumberExpression(0.0)
    )
): ParticleEmitterShape {
    companion object {
        val CODEC: Codec<PointParticleEmitterShape> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name },
                EXPRESSION_CODEC.fieldOf("offsetX").forGetter { it.offset.first },
                EXPRESSION_CODEC.fieldOf("offsetY").forGetter { it.offset.second },
                EXPRESSION_CODEC.fieldOf("offsetZ").forGetter { it.offset.third }
            ).apply(instance) { _, offsetX, offsetY, offsetZ -> PointParticleEmitterShape(Triple(offsetX, offsetY, offsetZ))}
        }
    }

    override val type = ParticleEmitterShapeType.POINT
    override fun getNewParticlePosition(runtime: MoLangRuntime, entity: Entity?) = runtime.resolveVec3d(offset)
    override fun getCenter(runtime: MoLangRuntime, entity: Entity?): Vec3 {
        return Vec3.ZERO
    }

    override fun <T> encode(ops: DynamicOps<T>) = CODEC.encodeStart(ops, this)
    override fun readFromBuffer(buffer: RegistryFriendlyByteBuf) {
        offset = Triple(
            MoLang.createParser(buffer.readString()).parseExpression(),
            MoLang.createParser(buffer.readString()).parseExpression(),
            MoLang.createParser(buffer.readString()).parseExpression()
        )
    }

    override fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(offset.first.getString())
        buffer.writeString(offset.second.getString())
        buffer.writeString(offset.third.getString())
    }
}

class BoxParticleEmitterShape(
    var offset: Triple<Expression, Expression, Expression> = Triple(
        NumberExpression(0.0),
        NumberExpression(0.0),
        NumberExpression(0.0)
    ),
    var boxSize: Triple<Expression, Expression, Expression> = Triple(
        NumberExpression(1.0),
        NumberExpression(1.0),
        NumberExpression(1.0)
    ),
    var surfaceOnly: Boolean = false
) : ParticleEmitterShape {
    companion object {
        val CODEC: Codec<BoxParticleEmitterShape> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name },
                EXPRESSION_CODEC.fieldOf("offsetX").forGetter { it.offset.first },
                EXPRESSION_CODEC.fieldOf("offsetY").forGetter { it.offset.second },
                EXPRESSION_CODEC.fieldOf("offsetZ").forGetter { it.offset.third },
                EXPRESSION_CODEC.fieldOf("sizeX").forGetter { it.boxSize.first },
                EXPRESSION_CODEC.fieldOf("sizeY").forGetter { it.boxSize.second },
                EXPRESSION_CODEC.fieldOf("sizeZ").forGetter { it.boxSize.third },
                PrimitiveCodec.BOOL.fieldOf("surfaceOnly").forGetter { it.surfaceOnly }
            ).apply(instance) { _, offsetX, offsetY, offsetZ, boxX, boxY,  boxZ, surfaceOnly ->
                BoxParticleEmitterShape(Triple(offsetX, offsetY, offsetZ), Triple(boxX, boxY, boxZ), surfaceOnly)
            }
        }
    }

    override val type = ParticleEmitterShapeType.BOX

    override fun <T> encode(ops: DynamicOps<T>): DataResult<T> {
        return CODEC.encodeStart(ops, this)
    }

    override fun readFromBuffer(buffer: RegistryFriendlyByteBuf) {
        offset = Triple(
            MoLang.createParser(buffer.readString()).parseExpression(),
            MoLang.createParser(buffer.readString()).parseExpression(),
            MoLang.createParser(buffer.readString()).parseExpression()
        )
        boxSize = Triple(
            MoLang.createParser(buffer.readString()).parseExpression(),
            MoLang.createParser(buffer.readString()).parseExpression(),
            MoLang.createParser(buffer.readString()).parseExpression()
        )
        surfaceOnly = buffer.readBoolean()
    }

    override fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(offset.first.getString())
        buffer.writeString(offset.second.getString())
        buffer.writeString(offset.third.getString())
        buffer.writeString(boxSize.first.getString())
        buffer.writeString(boxSize.second.getString())
        buffer.writeString(boxSize.third.getString())
        buffer.writeBoolean(surfaceOnly)
    }

    override fun getCenter(runtime: MoLangRuntime, entity: Entity?) = runtime.resolveVec3d(offset)

    override fun getNewParticlePosition(runtime: MoLangRuntime, entity: Entity?): Vec3 {
        val center = getCenter(runtime, entity)
        val sizes = runtime.resolveVec3d(boxSize).scale(2.0).add(0.0001, 0.0001, 0.0001)
        val disposition = if (surfaceOnly) {
            when (Random.Default.nextInt(6)) {
                0 -> Vec3(
                    -1 / 2.0 * sizes.x,
                    Random.nextDouble(sizes.y) - sizes.y / 2.0,
                    Random.nextDouble(sizes.z) - sizes.z / 2.0
                )
                1 -> Vec3(
                    1 / 2.0 * sizes.x,
                    Random.nextDouble(sizes.y) - sizes.y / 2.0,
                    Random.nextDouble(sizes.z) - sizes.z / 2.0
                )
                2 -> Vec3(
                    Random.nextDouble(sizes.x) - sizes.x / 2.0,
                    -1 / 2.0 * sizes.y,
                    Random.nextDouble(sizes.z) - sizes.z / 2.0
                )
                3 -> Vec3(
                    Random.nextDouble(sizes.x) - sizes.x / 2.0,
                    1 / 2.0 * sizes.y,
                    Random.nextDouble(sizes.z) - sizes.z / 2.0
                )
                4 -> Vec3(
                    Random.nextDouble(sizes.x) - sizes.x / 2.0,
                    Random.nextDouble(sizes.y) - sizes.y / 2.0,
                    -1 / 2.0 * sizes.z
                )
                else -> Vec3(
                    Random.nextDouble(sizes.x) - sizes.x / 2.0,
                    Random.nextDouble(sizes.y) - sizes.y / 2.0,
                    1 / 2.0 * sizes.z
                )
            }
        } else {
            Vec3(
                Random.nextDouble(sizes.x) - sizes.x / 2,
                Random.nextDouble(sizes.y) - sizes.y / 2,
                Random.nextDouble(sizes.z) - sizes.z / 2
            )
        }

        return center.add(disposition)
    }
}

class DiscParticleEmitterShape(
    var offset: Triple<Expression, Expression, Expression> = Triple(
        NumberExpression(0.0),
        NumberExpression(0.0),
        NumberExpression(0.0)
    ),
    var radius: Expression = 0.0.asExpression(),
    var normal: Triple<Expression, Expression, Expression> = Triple(
        NumberExpression(0.0),
        NumberExpression(1.0),
        NumberExpression(0.0)
    ),
    var surfaceOnly: Boolean = false
): ParticleEmitterShape {
    companion object {
        val CODEC: Codec<DiscParticleEmitterShape> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name },
                EXPRESSION_CODEC.fieldOf("offsetX").forGetter { it.offset.first },
                EXPRESSION_CODEC.fieldOf("offsetY").forGetter { it.offset.second },
                EXPRESSION_CODEC.fieldOf("offsetZ").forGetter { it.offset.third },
                EXPRESSION_CODEC.fieldOf("radius").forGetter { it.radius },
                EXPRESSION_CODEC.fieldOf("normalX").forGetter { it.normal.first },
                EXPRESSION_CODEC.fieldOf("normalY").forGetter { it.normal.second },
                EXPRESSION_CODEC.fieldOf("normalZ").forGetter { it.normal.third },
                PrimitiveCodec.BOOL.fieldOf("surfaceOnly").forGetter { it.surfaceOnly }
            ).apply(instance) { _, offsetX, offsetY, offsetZ, radius, normalX, normalY, normalZ, surfaceOnly ->
                DiscParticleEmitterShape(
                    offset = Triple(offsetX, offsetY, offsetZ),
                    radius = radius,
                    normal = Triple(normalX, normalY, normalZ),
                    surfaceOnly = surfaceOnly
                )
            }
        }
    }

    override val type = ParticleEmitterShapeType.DISC
    override fun getNewParticlePosition(runtime: MoLangRuntime, entity: Entity?): Vec3 {
        val center = getCenter(runtime, entity)
        val normal = runtime.resolveVec3d(normal).let { if (it == Vec3.ZERO) Vec3(
            0.0,
            1.0,
            0.0
        ) else it }.normalize()
        val baseLine = Vec3(0.0, 1.0, 0.0)
        val radius = runtime.resolveDouble(radius)
        val rotation = getRotationMatrix(from = baseLine, to = normal)
        val distance = if (surfaceOnly) radius else Random.Default.nextDouble(radius)
        val theta = Random.Default.nextDouble() * 2 * Math.PI
        // Polar to cartesian
        val x = distance * cos(theta)
        val z = distance * sin(theta)
        val displacement = rotation * Vec3(x, 0.0, z)
        return center.add(displacement)
    }

    override fun getCenter(runtime: MoLangRuntime, entity: Entity?) = runtime.resolveVec3d(offset)
    override fun <T> encode(ops: DynamicOps<T>) = CODEC.encodeStart(ops, this)
    override fun readFromBuffer(buffer: RegistryFriendlyByteBuf) {
        offset = Triple(
            MoLang.createParser(buffer.readString()).parseExpression(),
            MoLang.createParser(buffer.readString()).parseExpression(),
            MoLang.createParser(buffer.readString()).parseExpression()
        )
        radius = MoLang.createParser(buffer.readString()).parseExpression()
        normal = Triple(
            MoLang.createParser(buffer.readString()).parseExpression(),
            MoLang.createParser(buffer.readString()).parseExpression(),
            MoLang.createParser(buffer.readString()).parseExpression()
        )
        surfaceOnly = buffer.readBoolean()
    }

    override fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(offset.first.getString())
        buffer.writeString(offset.second.getString())
        buffer.writeString(offset.third.getString())
        buffer.writeString(radius.getString())
        buffer.writeString(normal.first.getString())
        buffer.writeString(normal.second.getString())
        buffer.writeString(normal.third.getString())
        buffer.writeBoolean(surfaceOnly)
    }
}

class EntityBoundingBoxParticleEmitterShape(
    var surfaceOnly: Boolean = true
): ParticleEmitterShape {
    companion object {
        val CODEC: Codec<EntityBoundingBoxParticleEmitterShape> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("type").forGetter { it.type.name },
                PrimitiveCodec.BOOL.fieldOf("surfaceOnly").forGetter { it.surfaceOnly }
            ).apply(instance) { _, surfaceOnly -> EntityBoundingBoxParticleEmitterShape(surfaceOnly = surfaceOnly)}
        }
    }

    override val type = ParticleEmitterShapeType.ENTITY_BOUNDING_BOX
    override fun getNewParticlePosition(runtime: MoLangRuntime, entity: Entity?): Vec3 {
        val box = getBox(entity)
        val center = getCenter(runtime, entity)
        val sizes = Vec3(
            box.maxX - box.minX,
            box.maxY - box.minY,
            box.maxZ - box.minZ
        )
        val disposition = if (surfaceOnly) {
            when (Random.Default.nextInt(6)) {
                0 -> Vec3(
                    -1 / 2.0 * sizes.x,
                    Random.nextDouble(sizes.y) - sizes.y / 2.0,
                    Random.nextDouble(sizes.z) - sizes.z / 2.0
                )
                1 -> Vec3(
                    1 / 2.0 * sizes.x,
                    Random.nextDouble(sizes.y) - sizes.y / 2.0,
                    Random.nextDouble(sizes.z) - sizes.z / 2.0
                )
                2 -> Vec3(
                    Random.nextDouble(sizes.x) - sizes.x / 2.0,
                    -1 / 2.0 * sizes.y,
                    Random.nextDouble(sizes.z) - sizes.z / 2.0
                )
                3 -> Vec3(
                    Random.nextDouble(sizes.x) - sizes.x / 2.0,
                    1 / 2.0 * sizes.y,
                    Random.nextDouble(sizes.z) - sizes.z / 2.0
                )
                4 -> Vec3(
                    Random.nextDouble(sizes.x) - sizes.x / 2.0,
                    Random.nextDouble(sizes.y) - sizes.y / 2.0,
                    -1 / 2.0 * sizes.z
                )
                else -> Vec3(
                    Random.nextDouble(sizes.x) - sizes.x / 2.0,
                    Random.nextDouble(sizes.y) - sizes.y / 2.0,
                    1 / 2.0 * sizes.z
                )
            }
        } else {
            Vec3(
                Random.nextDouble(sizes.x) - sizes.x / 2,
                Random.nextDouble(sizes.y) - sizes.y / 2,
                Random.nextDouble(sizes.z) - sizes.z / 2
            )
        }

        return center.add(disposition)
    }

    override fun getCenter(runtime: MoLangRuntime, entity: Entity?) = getBox(entity).center

    override fun <T> encode(ops: DynamicOps<T>) = CODEC.encodeStart(ops, this)
    fun getBox(entity: Entity?) = entity?.boundingBox ?: AABB.ofSize(
        Vec3(
            0.0,
            0.0,
            0.0
        ), 1.0, 2.0, 1.0)

    override fun readFromBuffer(buffer: RegistryFriendlyByteBuf) {
        surfaceOnly = buffer.readBoolean()
    }

    override fun writeToBuffer(buffer: RegistryFriendlyByteBuf) {
        buffer.writeBoolean(surfaceOnly)
    }
}