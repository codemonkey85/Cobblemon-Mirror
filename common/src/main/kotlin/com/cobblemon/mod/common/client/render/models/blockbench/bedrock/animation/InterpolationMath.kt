/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation

import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation.Companion.X_AXIS
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation.Companion.Y_AXIS
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation.Companion.Z_AXIS
import kotlin.math.floor
import net.minecraft.world.phys.Vec3

/**
 * Interpolates a vector based on a Catmull-Rom spline.
 * Frame A to D should be in order based on time. Frame A should be the keyframe before frame B, which should be the keyframe before C, and so on.
 */
fun catmullromLerp(frameA: BedrockAnimationKeyFrame?, frameB: BedrockAnimationKeyFrame, frameC: BedrockAnimationKeyFrame, frameD: BedrockAnimationKeyFrame?, time: Double, runtime: MoLangRuntime): Vec3 {
    return Vec3(
        catmullromLerp(frameA, frameB, frameC, frameD, X_AXIS, time, runtime),
        catmullromLerp(frameA, frameB, frameC, frameD, Y_AXIS, time, runtime),
        catmullromLerp(frameA, frameB, frameC, frameD, Z_AXIS, time, runtime)
    )
}

/**
 * Gets the interpolation alpha for a value between two points
 *
 * @return A linearly interpolated value between 0 and 1
 */
fun linearLerpAlpha(before: Double, after: Double, value: Double): Double {
    return (value - before) / (after - before)
}

/**
 * Interpolates a value based on a Catmull-Rom spline on a given axis.
 * Frame A to D should be in order based on time. Frame A should be the keyframe before frame B, which should be the keyframe before C, and so on.
 */
fun catmullromLerp(frameA: BedrockAnimationKeyFrame?, frameB: BedrockAnimationKeyFrame, frameC: BedrockAnimationKeyFrame, frameD: BedrockAnimationKeyFrame?, axis: Int, time: Double, runtime: MoLangRuntime) : Double {
    val vectors = mutableListOf<Vector2d>()
    val frameAData = frameA?.post?.resolve(time, runtime)
    val frameBData = frameB.post.resolve(time, runtime)
    val frameCData = frameC.pre.resolve(time, runtime)
    val frameDData = frameD?.pre?.resolve(time, runtime)
    if (frameAData != null) vectors.add(Vector2d(frameA.time, frameAData.get(axis)))
    vectors.add(Vector2d(frameB.time, frameBData.get(axis)))
    vectors.add(Vector2d(frameC.time, frameCData.get(axis)))
    if (frameDData != null) vectors.add(Vector2d(frameD.time, frameDData.get(axis)))
    val alpha = ((linearLerpAlpha(frameB.time, frameC.time, time)) + if (frameA != null) 1 else 0) / (vectors.size - 1)
    return getPointOnSpline(vectors, alpha).b
}

fun Vec3.get(axis: Int) = when (axis) {
    0 -> x
    1 -> y
    else -> z
}

private fun getPointOnSpline(points: List<Vector2d>, time: Double): Vector2d {
    val p = (points.size - 1) * time
    val intPoint = floor(p).toInt()
    val weight = p - intPoint

    val p0Index = if (intPoint == 0) intPoint else intPoint - 1
    val p2Index = if (intPoint > points.size - 2) points.size - 1 else intPoint + 1
    val p3Index = if (intPoint > points.size - 3) points.size - 1 else intPoint + 2

    val p0 = points[p0Index]
    val p1 = points[intPoint]
    val p2 = points[p2Index]
    val p3 = points[p3Index]

    return Vector2d(
        a = catmullrom(weight, p0.a, p1.a, p2.a, p3.a),
        b = catmullrom(weight, p0.b, p1.b, p2.b, p3.b)
    )
}

private fun catmullrom(t: Double, p0: Double, p1: Double, p2: Double, p3: Double): Double {
    val v0 = (p2 - p0) * 0.5
    val v1 = (p3 - p1) * 0.5
    val t2 = t * t
    val t3 = t * t2
    return (2 * p1 - 2 * p2 + v0 + v1) * t3 + (-3 * p1 + 3 * p2 - 2 * v0 - v1) * t2 + v0 * t + p1
}

private data class Vector2d(val a: Double, val b: Double)