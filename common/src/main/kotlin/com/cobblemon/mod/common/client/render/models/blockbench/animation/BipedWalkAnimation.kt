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
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BipedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Bone
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation.Companion.X_AXIS
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import net.minecraft.util.Mth

/**
 * A biped animation that will have zero-rotations on all legs at
 * zero and otherwise does simple predictable walking like Minecraft
 * quadrupeds.
 *
 * @author Deltric
 * @since December 21st, 2021
 */
class BipedWalkAnimation(
    /** The multiplier to apply to the cosine movement of the legs. The smaller this value, the quicker the legs move. */
    val periodMultiplier: Float = 0.6662F,
    /** The multiplier to apply to the stride of the entity. The larger this is, the further the legs move. */
    val amplitudeMultiplier: Float = 1.4F,
    val leftLeg: Bone?,
    val rightLeg: Bone?
) : PoseAnimation() {
    constructor(
        frame: BipedFrame,
        periodMultiplier: Float = 0.6662F,
        amplitudeMultiplier: Float = 1.4F
    ): this(
        periodMultiplier = periodMultiplier,
        amplitudeMultiplier = amplitudeMultiplier,
        leftLeg = frame.leftLeg,
        rightLeg = frame.rightLeg
    )

    override fun setupAnim(context: RenderContext, model: PosableModel, state: PosableState, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, headYaw: Float, headPitch: Float, intensity: Float) {
        rightLeg?.addRotation(X_AXIS, Mth.cos(limbSwing * periodMultiplier + Math.PI.toFloat()) * limbSwingAmount * amplitudeMultiplier * intensity)
        leftLeg?.addRotation(X_AXIS, Mth.cos(limbSwing * periodMultiplier) * limbSwingAmount * amplitudeMultiplier * intensity)
    }
}