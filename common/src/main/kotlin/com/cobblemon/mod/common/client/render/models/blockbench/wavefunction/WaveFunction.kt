/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.wavefunction

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import net.minecraft.util.Mth.PI
import net.minecraft.util.Mth.sqrt

typealias WaveFunction = (Float) -> Float
typealias CoFunction = (Float) -> Pair<Float, Float>
typealias TriFunction = (Float) -> Triple<Float, Float, Float>

fun gradient(function: WaveFunction, t0: Float, t1: Float) = (function(t1) - function(t0)) / (t1 - t0)

operator fun WaveFunction.plus(other: WaveFunction): WaveFunction = { t -> this(t) + other(t) }
operator fun WaveFunction.times(other: WaveFunction): WaveFunction = { t -> this(t) * other(t) }
fun WaveFunction.rerange(min: Float, max: Float): WaveFunction = { t ->
    if (t in min..max) {
        val newTime = (t - min) / (max - min)
        this(newTime)
    } else {
        this(0F)
    }
}
fun WaveFunction.shift(shift: Float): WaveFunction = { t -> this(t + shift) }
fun WaveFunction.timeDilate(dilation: Float): WaveFunction = { t -> this(t * dilation) }
fun WaveFunction.min(other: Float): WaveFunction = { t -> minOf(this(t), other) }
fun WaveFunction.max(other: Float): WaveFunction = { t -> maxOf(this(t), other) }
fun WaveFunction.clamp(min: Float, max: Float): WaveFunction = { t -> this(t).coerceIn(min, max) }
fun WaveFunction.aggregate(func: WaveFunction): WaveFunction = { t -> func(this(t)) }
fun linearFunction(gradient: Float = 1F, yIntercept: Float = 0F): WaveFunction = { t -> gradient * t + yIntercept }
fun sineFunction(amplitude: Float = 1F, period: Float = 1F, phaseShift: Float = 0F, verticalShift: Float = 0F): WaveFunction = { t -> sin(2*PI/period * (t - phaseShift)) * amplitude + verticalShift }
fun cosineFunction(amplitude: Float = 1F, period: Float = 1F, phaseShift: Float = 0F, verticalShift: Float = 0F): WaveFunction = { t -> cos(2*PI/period * (t - phaseShift)) * amplitude + verticalShift }
fun triangleFunction(amplitude: Float = 1F, period: Float = 1F, phaseShift: Float = 0F, verticalShift: Float = 0F): WaveFunction = { t ->
    var time = t
    while (time < 0) {
        time += period
    }
    val timeTerm = ((time + 3 * period / 4 - phaseShift) % period) - period/2
    val value = 4 * amplitude / period * abs(timeTerm) - amplitude + verticalShift
    value
}

/**
 * Returns a parabola that intercepts the y axis at zero, intercepts the x axis again
 * at [period], and at its furthest from y = 0 will reach [peak].
 *
 * The maths used to calculate this was a bit confusing but I've added my working to the
 * docs/parabolaworking.pdf file in the Cobblemon repository.
 */
fun parabolaFunction(
    peak: Float,
    period: Float
) = parabolaFunction(
    tightness = -4 * peak / period.pow(2),
    verticalShift = peak,
    phaseShift = period / 2
)

fun parabolaFunction(
    tightness: Float = -1F,
    phaseShift: Float = 0F,
    verticalShift: Float = 1F
): WaveFunction {
    /*
     * To understand the parameters for this, type y=t(x-p)^2+v into https://www.desmos.com/calculator and play
     * around with tightness, phase shift, and vertical shift
     */

    val a = tightness
    val b = -2 * phaseShift * tightness
    val c = tightness * phaseShift * phaseShift + verticalShift

    val root1 = (-b - sqrt(b*b - 4 * a * c)) / (2 * tightness)
    val root2 = (-b + sqrt(b*b - 4 * a * c)) / (2 * tightness)
    val tMin = if (root1 < root2) root1 else root2
    val tMax = if (root1 < root2) root2 else root1
    val period = tMax - tMin

    return { t ->
        var time = t
        while (time < tMin) {
            time += period
        }
        while (time > tMax) {
            time -= period
        }

        tightness * (time - phaseShift).pow(2) + verticalShift
    }
}

object WaveFunctions {
    val functions = mutableMapOf<String, WaveFunction>(
        "symmetrical" to { t ->
            if (t < 0.1) {
                t * 10
            } else if (t < 0.9) {
                1F
            } else {
                1F - (t - 0.9F) * 10
            }
        },
        "symmetrical_wide" to { t ->
            if (t < 0.2) {
                val t2 = t * 5
                0.5F * sin(PI*(t2 - 0.5F)) + 0.5F
            } else if (t < 0.8) {
                1F
            } else {
                val t2 = (1 - t) * 5
                0.5F * sin(PI*(t2 - 0.5F)) + 0.5F
            }
        },
        "one" to { 1F }
    )
}