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

/**
 * For conversion from BlockPos to Vec3d */
fun BlockPos.toVec3d(): Vec3 {
    return Vec3(this.x.toDouble(), this.y.toDouble(), this.z.toDouble())
}