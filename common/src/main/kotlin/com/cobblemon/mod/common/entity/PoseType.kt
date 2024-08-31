/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity

import com.cobblemon.mod.common.util.cobblemonResource
import java.util.*
import net.minecraft.resources.ResourceLocation

/**
 * The type of a pose. Used for normalizing pose swapping for all models.
 *
 * @author Hiroku
 * @since December 5th, 2021
 */
enum class PoseType {
    STAND,
    WALK,
    SLEEP,
    HOVER,
    FLY,
    FLOAT,
    SWIM,
    /** A pose for rendering on the left shoulder. Pose animations are given the player head yaw, pitch, and ageInTicks. */
    SHOULDER_LEFT,
    /** A pose for rendering on the right shoulder. Pose animations are given the player head yaw, pitch, and ageInTicks. */
    SHOULDER_RIGHT,
    /** A pose for rendering in the SummaryUI */
    PROFILE,
    /** A pose for rendering in the party overlay and in minor spaces like the battle tiles. */
    PORTRAIT,
    /** For guilded chest */
    OPEN,
    /** A simple type for non-living entities or errant cases. */
    NONE;

    companion object {
        val ALL_POSES = EnumSet.allOf(PoseType::class.java)
        val FLYING_POSES = EnumSet.of(FLY, HOVER)
        val SWIMMING_POSES = EnumSet.of(SWIM, FLOAT)
        val STANDING_POSES = EnumSet.of(STAND, WALK)
        val SHOULDER_POSES = EnumSet.of(SHOULDER_LEFT, SHOULDER_RIGHT)
        val UI_POSES = EnumSet.of(PROFILE, PORTRAIT)
        val MOVING_POSES = EnumSet.of(WALK, SWIM, FLY)
        val STATIONARY_POSES = EnumSet.of(STAND, FLOAT, HOVER)

        fun identified() : List<ResourceLocation> {
            return ALL_POSES.stream()
                .map { cobblemonResource(it.name.lowercase(Locale.getDefault())) }
                .toList()
        }
    }
}