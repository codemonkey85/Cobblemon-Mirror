/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.animation

import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.addRotation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.QuadrupedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Bone
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation.Companion.X_AXIS
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import net.minecraft.util.Mth

/**
 * A quadruped animation that will have zero-rotations on all legs at
 * zero and otherwise does simple predictable walking like Minecraft
 * quadrupeds.
 *
 * @author Hiroku
 * @since December 4th, 2021
 */
class QuadrupedWalkAnimation(
    val legFrontLeft: Bone?,
    val legFrontRight: Bone?,
    val legBackLeft: Bone?,
    val legBackRight: Bone?,
    /** The multiplier to apply to the cosine movement of the legs. The smaller this value, the quicker the legs move. */
    val periodMultiplier: Float = 0.6662F,
    /** The multiplier to apply to the stride of the entity. The larger this is, the further the legs move. */
    val amplitudeMultiplier: Float = 1.4F
) : PoseAnimation() {
    constructor(
        frame: QuadrupedFrame,
        periodMultiplier: Float = 0.6662F,
        amplitudeMultiplier: Float = 1.4F
    ): this(
        legFrontLeft = frame.foreLeftLeg,
        legFrontRight = frame.foreRightLeg,
        legBackLeft = frame.hindLeftLeg,
        legBackRight = frame.hindRightLeg,
        periodMultiplier = periodMultiplier,
        amplitudeMultiplier = amplitudeMultiplier
    )

    override fun setupAnim(context: RenderContext, model: PosableModel, state: PosableState, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, headYaw: Float, headPitch: Float, intensity: Float) {
        val hindRightLeg = legBackRight ?: return
        val hindLeftLeg = legBackLeft ?: return
        val foreRightLeg = legFrontRight ?: return
        val foreLeftLeg = legFrontLeft ?: return

        hindRightLeg.addRotation(X_AXIS, Mth.cos(limbSwing * periodMultiplier) * limbSwingAmount * amplitudeMultiplier * intensity)
        hindLeftLeg.addRotation(X_AXIS, Mth.cos(limbSwing * periodMultiplier + Math.PI.toFloat()) * limbSwingAmount * amplitudeMultiplier * intensity)
        foreRightLeg.addRotation(X_AXIS, Mth.cos(limbSwing * periodMultiplier + Math.PI.toFloat()) * limbSwingAmount * amplitudeMultiplier * intensity)
        foreLeftLeg.addRotation(X_AXIS, Mth.cos(limbSwing * periodMultiplier) * limbSwingAmount * amplitudeMultiplier * intensity)
    }
}