/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.frame

import com.cobblemon.mod.common.client.render.models.blockbench.animation.SingleBoneLookAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Bone

interface HeadedFrame : ModelFrame {
    val head: Bone

    fun singleBoneLook(invertX: Boolean = false, invertY: Boolean = false, disableX: Boolean = false, disableY: Boolean = false, pitchMultiplier: Float? = null, yawMultiplier: Float? = null, maxPitch: Float? = null, minPitch: Float? = null, maxYaw: Float? = null, minYaw: Float? = null) = SingleBoneLookAnimation(this, invertX, invertY, disableX, disableY, pitchMultiplier, yawMultiplier, maxPitch, minPitch, maxYaw, minYaw)
}