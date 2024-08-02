/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.animation

import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.PoseableEntityState
import com.cobblemon.mod.common.client.render.models.blockbench.addRotation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BiWingedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.ModelFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Bone
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.WaveFunction
import net.minecraft.entity.Entity
import net.minecraft.item.BoneMealItem

class WingFlapIdleAnimation<T : Entity>(
    frame: ModelFrame,
    val leftWing: Bone?,
    val rightWing: Bone?,
    val rotation: WaveFunction,
    val timeVariable: (state: PoseableEntityState<T>?, limbSwing: Float, ageInTicks: Float) -> Float? = { state, _, _ -> state?.animationSeconds ?: 0F },
    val axis: Int
) : StatelessAnimation<T, ModelFrame>(frame) {
    constructor(
        frame: BiWingedFrame,
        flapFunction: WaveFunction,
        timeVariable: (state: PoseableEntityState<T>?, limbSwing: Float, ageInTicks: Float) -> Float?,
        axis: Int
    ): this(
        frame = frame,
        leftWing = frame.leftWing,
        rightWing = frame.rightWing,
        rotation = flapFunction,
        timeVariable = timeVariable,
        axis = axis
    )

    override val targetFrame = ModelFrame::class.java

    override fun setAngles(entity: T?, model: PoseableEntityModel<T>, state: PoseableEntityState<T>?, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, headYaw: Float, headPitch: Float, intensity: Float) {
        val time = timeVariable(state, limbSwing, ageInTicks) ?: 0F
        val angle = rotation(time)
        leftWing?.addRotation(axis, angle)
        rightWing?.addRotation(axis, angle)
    }
}