/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.math.geometry

import net.minecraft.world.phys.Vec3

/**
 * A three dimensional point in space.
 *
 * Often used in conjunction with [TransformationMatrix] for three dimensional transformations.
 *
 * @author landonjw
 */
data class GeometricPoint(val x: Float, val y: Float, val z: Float) {

    val w: Float = 1f // This is always one for translations used within transformation matrices

    operator fun plus(right: GeometricPoint): GeometricPoint = add(this, right)
    operator fun times(scalar: Float): GeometricPoint = multiply(this, scalar)

    fun toVec3d() = Vec3(x.toDouble(), y.toDouble(), z.toDouble())

    constructor() : this(0f, 0f, 0f)
    constructor(x: Double, y: Double, z: Double): this(x.toFloat(), y.toFloat(), z.toFloat())
    constructor(vec3d: Vec3) : this(vec3d.x, vec3d.y, vec3d.z)

    companion object {

        /**
         * Creates a new geometric point representing two points added together.
         *
         * @param left the point to add
         * @param right the point to add
         * @return point representing sum of two other points
         */
        fun add(left: GeometricPoint, right: GeometricPoint): GeometricPoint {
            return GeometricPoint(left.x + right.x, left.y + right.y, left.z + right.z)
        }

        /**
         * Creates a new geometric point representing a product of a point multiplied by a scalar
         *
         * @param point the point to multiply
         * @return point representing product of a point and scalar
         */
        fun multiply(point: GeometricPoint, scalar: Float): GeometricPoint {
            return GeometricPoint(point.x * scalar, point.y * scalar, point.z * scalar)
        }

    }

}