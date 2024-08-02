/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f

/**
 * For conversion from Vec3dto BlockPos, loses accuracy
 */
fun Vec3.toBlockPos(): BlockPos {
    return BlockPos.containing(this.x, this.y, this.z)
}

fun Vec3.toVec3f(): Vector3f = Vector3f(x.toFloat(), y.toFloat(), z.toFloat())
fun Vector3f.toVec3d(): Vec3 =
    Vec3(x.toDouble(), y.toDouble(), z.toDouble())
fun Vector3f.set(vec3d: Vec3): Vector3f {
    x = vec3d.x.toFloat()
    y = vec3d.y.toFloat()
    z = vec3d.z.toFloat()
    return this
}