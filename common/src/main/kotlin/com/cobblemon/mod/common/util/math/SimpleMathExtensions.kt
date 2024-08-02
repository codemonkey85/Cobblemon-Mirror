/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.math

import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random
import net.minecraft.world.phys.Vec3
import org.joml.Matrix3f
import org.joml.Quaternionf
import org.joml.Vector3f

infix fun Int.pow(power: Int): Int {
    return toDouble().pow(power.toDouble()).toInt()
}

fun Double?.orMax() = this ?: Double.MAX_VALUE
fun Double?.orMin() = this ?: Int.MIN_VALUE.toDouble()
fun Float?.orMax() = this ?: Int.MAX_VALUE.toFloat()
fun Float?.orMin() = this ?: Int.MIN_VALUE.toFloat()
fun Int?.orMax() = this ?: Int.MAX_VALUE
fun Int?.orMin() = this ?: Int.MIN_VALUE

fun Int.toRGB(): Triple<Double, Double, Double> {
    val r = ((this shr 16) and 0b11111111) / 255.0
    val g = ((this shr 8) and 0b11111111) / 255.0
    val b = (this and 0b11111111) / 255.0
    return Triple(r, g, b)
}

fun IntRange.intersects(other: IntRange) = start in other || endInclusive in other || other.start in this
fun IntRange.intersection(other: IntRange): IntRange {
    val intersectionStart = max(other.start, start)
    val intersectionEnd = min(other.endInclusive, endInclusive)

    return intersectionStart..intersectionEnd
}

fun Pair<Float, Float>.random() = Random.nextFloat() * (second - first) + first

fun Float.remap(from: Pair<Float, Float>, to: Pair<Float, Float>): Float {
    val (fromMin, fromMax) = from
    val (toMin, toMax) = to
    return (this - fromMin) / (fromMax - fromMin) * (toMax - toMin) + toMin
}

fun Float.remap(start: FloatRange, end: FloatRange): Float{
    val (fromMin, fromMax) = start.start to start.endInclusive
    val (toMin, toMax) = end.start to end.endInclusive
    return (this - fromMin) / (fromMax - fromMin) * (toMax - toMin) + toMin
}

fun Int.remap(from: Pair<Int, Int>, to: Pair<Int, Int>): Int {
    val (fromMin, fromMax) = from
    val (toMin, toMax) = to
    return (this - fromMin) / (fromMax - fromMin) * (toMax - toMin) + toMin
}

fun Int.remap(start: IntRange, end: IntRange): Int{
    val (fromMin, fromMax) = start.first to start.last
    val (toMin, toMax) = end.first to end.last
    return (this - fromMin) / (fromMax - fromMin) * (toMax - toMin) + toMin
}

fun Double.remap(from: Pair<Double, Double>, to: Pair<Double, Double>): Double {
    val (fromMin, fromMax) = from
    val (toMin, toMax) = to
    return (this - fromMin) / (fromMax - fromMin) * (toMax - toMin) + toMin
}

fun Double.remap(start: DoubleRange, end: DoubleRange): Double{
    val (fromMin, fromMax) = start.start to start.endInclusive
    val (toMin, toMax) = end.start to end.endInclusive
    return (this - fromMin) / (fromMax - fromMin) * (toMax - toMin) + toMin
}



class FloatRange(override val start: Float, override val endInclusive: Float) : ClosedFloatingPointRange<Float> {
    override fun contains(value: Float): Boolean = value in start..endInclusive
    override fun isEmpty(): Boolean = start > endInclusive
    override fun lessThanOrEquals(a: Float, b: Float): Boolean {
        return a <= b
    }
    override fun equals(other: Any?): Boolean = other is FloatRange && start == other.start && endInclusive == other.endInclusive
    override fun hashCode(): Int = 31 * start.hashCode() + endInclusive.hashCode()

    override fun toString(): String = "$start..$endInclusive"
}

class DoubleRange(override val start: Double, override val endInclusive: Double) : ClosedFloatingPointRange<Double> {
    override fun contains(value: Double): Boolean = value in start..endInclusive
    override fun isEmpty(): Boolean = start > endInclusive
    override fun lessThanOrEquals(a: Double, b: Double): Boolean {
        return a <= b
    }
    override fun equals(other: Any?): Boolean = other is DoubleRange && start == other.start && endInclusive == other.endInclusive
    override fun hashCode(): Int = 31 * start.hashCode() + endInclusive.hashCode()

    override fun toString(): String = "$start..$endInclusive"
}

fun convertSphericalToCartesian(radius: Double, theta: Double, psi: Double): Vec3 =
    Vec3(
        radius * cos(theta) * sin(psi),
        radius * sin(theta) * sin(psi),
        radius * cos(psi)
    )

/** Based on [this answer](https://math.stackexchange.com/questions/180418/calculate-rotation-matrix-to-align-vector-a-to-vector-b-in-3d) */
fun getRotationMatrix(from: Vec3, to: Vec3): Matrix3f {

    val q = Quaternionf(0.0, 0.0, 0.0, 1.0)
    q.rotateTo(Vector3f(from.x.toFloat(), from.y.toFloat(), from.z.toFloat()), Vector3f(to.x.toFloat(), to.y.toFloat(), to.z.toFloat()))
    val m = Matrix3f()
    return m.identity().rotation(q)
    /**
    val v = from.crossProduct(to)
    val c = from.dotProduct(to)

    val identity = Matrix3f().identity()

    if (c == -1.0) {
        identity.scale(-1F)
        return identity
    } else if (c == 1.0) {
        return identity
    }

    val vx = Matrix3f()
    vx.set(0, 1, -v.z.toFloat())
    vx.set(0, 2, v.y.toFloat())
    vx.set(1, 0, v.z.toFloat())
    vx.set(1, 2, -v.x.toFloat())
    vx.set(2, 0, -v.y.toFloat())
    vx.set(2, 1, v.x.toFloat())

    val vx2 = Matrix3f(vx)
    vx2.mul(vx2)

    val r = Matrix3f(identity)
    vx2.scale((1 / (1 + c)).toFloat())
    r.add(vx)
    r.add(vx2)

    return r
    **/
}

operator fun Matrix3f.times(vec3d: Vec3): Vec3 {
    val vec3f = Vector3f()
    this.transform(vec3d.x.toFloat(), vec3d.y.toFloat(), vec3d.z.toFloat(), vec3f)
    return Vec3(vec3f.x.toDouble(), vec3f.y.toDouble(), vec3f.z.toDouble())
//    return Vec3d(
//        a00 * vec3d.x + a01 * vec3d.y + a02 * vec3d.z,
//        a10 * vec3d.x + a11 * vec3d.y + a12 * vec3d.z,
//        a20 * vec3d.x + a21 * vec3d.y + a22 * vec3d.z
//    )
}