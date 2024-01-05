/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.animation

import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.addRotation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation.Companion.X_AXIS
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation.Companion.Y_AXIS
import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.entity.Entity

/**
 * A very simple animation for [HeadedFrame]s which has the entity look along the head yaw and pitch.
 * This is designed for simple entities where the model only needs to move a single bone to look at a
 * target.
 *
 * @author Hiroku
 * @since December 5th, 2021
 */
class SingleBoneLookAnimation<T : Entity>(frame: HeadedFrame, val invertX: Boolean, val invertY: Boolean, val disableX: Boolean, val disableY: Boolean) : StatelessAnimation(frame) {
    override val targetFrame: Class<HeadedFrame> = HeadedFrame::class.java
    override var labels = setOf("look")
    override fun setAngles(entity: T?, model: PosableModel, state: PosableState?, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, headYaw: Float, headPitch: Float, intensity: Float) {
        val pitch = (if (invertX) -1 else 1) * headPitch.coerceIn(-45F, 70F)
        val yaw = (if (invertY) -1 else 1) * headYaw.coerceIn(-45F, 45F)

        (if (!disableX) frame.head.addRotation(X_AXIS, pitch.toRadians() * intensity))
        (if (!disableY) frame.head.addRotation(Y_AXIS, yaw.toRadians() * intensity))
    }
}