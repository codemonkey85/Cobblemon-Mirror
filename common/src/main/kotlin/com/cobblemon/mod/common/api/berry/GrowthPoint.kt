/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.berry

import net.minecraft.world.phys.Vec3

/**
 * Represents a position in a berry tree where a berry will flower then fruit.
 *
 * @property position The coordinates of the berry in the tree block.
 * @property rotation The degree value for the rotation each axis.
 *
 * @author Licious
 * @since December 19th, 2022
 */
data class GrowthPoint(
    val position: Vec3,
    val rotation: Vec3
)
