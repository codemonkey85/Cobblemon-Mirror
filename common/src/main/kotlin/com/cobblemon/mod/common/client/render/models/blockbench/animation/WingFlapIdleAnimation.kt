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
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BiWingedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Bone
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.WaveFunction

class WingFlapIdleAnimation(
    val leftWing: Bone?,
    val rightWing: Bone?,
    val rotation: WaveFunction,
    val timeVariable: (state: PosableState, limbSwing: Float, ageInTicks: Float) -> Float? = { state, _, _ -> state.animationSeconds },
    val axis: Int
) : PoseAnimation() {
    constructor(
        frame: BiWingedFrame,
        flapFunction: WaveFunction,
        timeVariable: (state: PosableState, limbSwing: Float, ageInTicks: Float) -> Float?,
        axis: Int
    ): this(
        leftWing = frame.leftWing,
        rightWing = frame.rightWing,
        rotation = flapFunction,
        timeVariable = timeVariable,
        axis = axis
    )

    override fun setupAnim(context: RenderContext, model: PosableModel, state: PosableState, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, headYaw: Float, headPitch: Float, intensity: Float) {
        val time = timeVariable(state, limbSwing, ageInTicks) ?: 0F
        val angle = rotation(time)
        leftWing?.addRotation(axis, angle)
        rightWing?.addRotation(axis, -angle)
    }
}