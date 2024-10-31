/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.math.geometry

import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import org.joml.Vector4f

fun Matrix4f.getOrigin(): Vec3 {
    return transformPosition(Vec3.ZERO)
}

fun Matrix4f.transformPosition(pos: Vec3): Vec3 {
    val vector = Vector4f(pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat(), 1F)
    transform(vector)
    vector.mul(1 / vector.w)
    return Vec3(
        vector.x.toDouble(),
        vector.y.toDouble(),
        vector.z.toDouble()
    )
}

fun Matrix4f.transformDirection(direction: Vec3): Vec3 {
    val origin = Vector4f(0F, 0F, 0F, 1F)
    transform(origin)
    origin.mul(1 / origin.w)
    val originVec =
        Vec3(origin.x.toDouble(), origin.y.toDouble(), origin.z.toDouble())
    val magnitude = direction.length()
    val vector = Vector4f(direction.x.toFloat(), direction.y.toFloat(), direction.z.toFloat(), 1F)
    this.transform(vector)
    vector.mul(1 / vector.w)
    return Vec3(
        vector.x.toDouble(),
        vector.y.toDouble(),
        vector.z.toDouble()
    )
        .subtract(originVec)
        .normalize()
        .scale(magnitude)
}