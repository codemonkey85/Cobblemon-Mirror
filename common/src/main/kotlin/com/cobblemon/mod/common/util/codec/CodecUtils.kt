/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.codec

import com.bedrockk.molang.Expression
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.util.asExpressionLike
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.world.phys.AABB

object CodecUtils {

    @JvmStatic
    fun <T> createByIdentifierCodec(from: (ResourceLocation) -> T?, to: (T) -> ResourceLocation, errorSupplier: (ResourceLocation) -> String): Codec<T> {
        return ResourceLocation.CODEC.comapFlatMap(
            { identifier -> from(identifier)?.let { value -> DataResult.success(value) } ?: DataResult.error { errorSupplier(identifier) } },
            to
        )
    }

    @JvmStatic
    fun <T> createByStringCodec(from: (String) -> T?, to: (T) -> String, errorSupplier: (String) -> String): Codec<T> {
        return Codec.STRING.comapFlatMap(
            { string -> from(string)?.let { value -> DataResult.success(value) } ?: DataResult.error { errorSupplier(string) } },
            to
        )
    }

    /**
     * Generates a [Int] [Codec] that has a range check that uses dynamic values.
     *
     * Useful to check against a reloadable config value(s).
     *
     * @param min The supplier for the min.
     * @param max The supplier for the max.
     * @return The generated [Codec].
     */
    @JvmStatic
    fun dynamicIntRange(min: () -> Int, max: () -> Int): Codec<Int> {
        val checker = this.dynamicRangeChecker(min, max)
        return Codec.INT.flatXmap(
            { checker(it) },
            { checker(it) }
        )
    }

    /**
     * @see [CodecUtils.dynamicIntRange]
     */
    @JvmStatic
    fun dynamicIntRange(min: Int, max: () -> Int): Codec<Int> = dynamicIntRange({ min }, max)

    /**
     * @see [CodecUtils.dynamicIntRange]
     */
    @JvmStatic
    fun dynamicIntRange(min: () -> Int, max: Int): Codec<Int> = dynamicIntRange(min) { max }

    fun <T> setOf(codec: Codec<T>): Codec<Set<T>> = codec.listOf()
        .xmap({ it.toSet() }, { it.toList() })

    @JvmStatic
    val ENTITY_DIMENSION: Codec<EntityDimensions> = RecordCodecBuilder.create { instance ->
        instance.group(
            ExtraCodecs.POSITIVE_FLOAT.fieldOf("width").forGetter(EntityDimensions::width),
            ExtraCodecs.POSITIVE_FLOAT.fieldOf("height").forGetter(EntityDimensions::height),
            Codec.BOOL.optionalFieldOf("fixed", false).forGetter(EntityDimensions::fixed)
        ).apply(instance) { width, height, fixed -> if (fixed) EntityDimensions.fixed(width, height) else EntityDimensions.scalable(width, height) }
    }

    @JvmStatic
    val INT_RANGE: Codec<IntRange> = intRange(Codec.INT)

    fun intRange(codec: Codec<Int>): Codec<IntRange> = Codec.list(codec, 2, 2)
        .xmap(
            { list -> IntRange(list[0], list[1]) },
            { range -> listOf(range.first, range.last) }
        )

    @JvmStatic
    val BOX: Codec<AABB> = RecordCodecBuilder.create { instance ->
        instance.group(
            Codec.DOUBLE.fieldOf("minX").forGetter(AABB::minX),
            Codec.DOUBLE.fieldOf("minY").forGetter(AABB::minY),
            Codec.DOUBLE.fieldOf("minZ").forGetter(AABB::minZ),
            Codec.DOUBLE.fieldOf("maxX").forGetter(AABB::maxX),
            Codec.DOUBLE.fieldOf("maxY").forGetter(AABB::maxY),
            Codec.DOUBLE.fieldOf("maxZ").forGetter(AABB::maxZ),
        ).apply(instance, ::AABB)
    }

    private fun dynamicRangeChecker(min: () -> Int, max: () -> Int): (Int) -> DataResult<Int> = { number ->
        val minAsNum = min()
        val maxAsNum = max()
        if (minAsNum >= maxAsNum) {
            DataResult.error { "The current dynamic range is invalid [$minAsNum:$maxAsNum]" }
        } else if (number in minAsNum..maxAsNum) {
            DataResult.success(number)
        } else {
            DataResult.error { "$number is not in range [$minAsNum:$maxAsNum]" }
        }
    }

}