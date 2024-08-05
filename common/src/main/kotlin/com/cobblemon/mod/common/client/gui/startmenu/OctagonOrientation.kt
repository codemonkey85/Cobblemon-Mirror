/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.startmenu

import net.minecraft.world.phys.Vec2

/**
 * Contains constants for each octagonal direction.
 *
 * @param vec2 A normalized vector in the specific direction.
 * @param angle Used for mouse position calculations, use [ordinal] instead for
 * the order used to add buttons to [OctagonGUI].
 */
enum class OctagonOrientation(val vec2: Vec2, val angle: Int) {
    TOP(Vec2(0F, -1F).normalized(), 2),
    LEFT(Vec2(-1F, 0F).normalized(), 0),
    RIGHT(Vec2(1F, 0F).normalized(),4),
    BOTTOM(Vec2(0F, 1F).normalized(),6),
    TOP_LEFT(Vec2(-1F, -1F).normalized(), 1),
    TOP_RIGHT(Vec2(1F, -1F).normalized(), 3),
    BOTTOM_LEFT(Vec2(-1F, 1F).normalized(),7),
    BOTTOM_RIGHT(Vec2(1F, 1F).normalized(),5),
}