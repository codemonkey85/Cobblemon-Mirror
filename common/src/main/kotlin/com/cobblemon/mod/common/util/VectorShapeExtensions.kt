/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import net.minecraft.core.Direction
import net.minecraft.core.Direction.EAST
import net.minecraft.core.Direction.NORTH
import net.minecraft.core.Direction.SOUTH
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

fun voxelShape(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double, direction: Direction): VoxelShape {
    val fMinX = when (direction) {
        NORTH -> minX
        SOUTH -> 1 - maxX
        EAST -> minZ
        else -> 1 - maxZ
    }

    val fMaxX = when (direction) {
        NORTH -> maxX
        SOUTH -> 1 - minX
        EAST -> maxZ
        else -> 1 - minZ
    }

    val fMinZ = when (direction) {
        NORTH -> minZ
        SOUTH -> 1 - maxZ
        EAST -> minX
        else -> 1 - maxX
    }

    val fMaxZ = when (direction) {
        NORTH -> maxZ
        SOUTH -> 1 - minZ
        EAST -> maxX
        else -> 1 - minX
    }

    return Shapes.box(fMinX, minY, fMinZ, fMaxX, maxY, fMaxZ)
}