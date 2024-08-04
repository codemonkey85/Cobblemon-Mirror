/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.condition

import net.minecraft.world.level.Level

/**
 * Represents the literal name of a moon phase instead of a raw number.
 * For more information see the [Minecraft wiki](https://minecraft.fandom.com/wiki/Moon#Phases) page.
 *
 * @author Licious
 * @since January 25th, 2023
 */
enum class MoonPhase {

    FULL_MOON,
    WANING_GIBBOUS,
    THIRD_QUARTER,
    WANING_CRESCENT,
    NEW_MOON,
    WAXING_CRESCENT,
    FIRST_QUARTER,
    WAXING_GIBBOUS;

    companion object {
        private val VALUES = values()

        /**
         * Finds the moon phase of the given [world].
         *
         * @param world The [Level] being queried.
         * @return The [MoonPhase] of the world.
         *
         * @throws IndexOutOfBoundsException if the moon phase cannot be resolved, this should never happen.
         */
        fun ofWorld(world: Level): MoonPhase = VALUES[world.moonPhase]
    }

}