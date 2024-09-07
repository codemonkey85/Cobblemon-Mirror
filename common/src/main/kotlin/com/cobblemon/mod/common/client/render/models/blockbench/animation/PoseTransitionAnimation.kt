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
import com.cobblemon.mod.common.client.render.models.blockbench.frame.ModelFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.WaveFunction
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.sineFunction
import java.lang.Float.min

/**
 * An animation that gradually moves any [ModelFrame] from one pose to another.
 *
 * @author Hiroku
 * @since December 5th, 2021
 */
class PoseTransitionAnimation(
    val beforePose: Pose,
    val afterPose: Pose,
    val durationTicks: Int = 20,
    val curve: WaveFunction = sineFunction(amplitude = 0.5F, period = 2F, phaseShift = 0.5F, verticalShift = 0.5F)
) : ActiveAnimation {
    override val isTransition = true
    override val enduresPrimaryAnimations = false
    override val duration: Float = durationTicks / 20F

    var initialized = false
    var startTime = 0F
    var endTime = 0F// startTime + durationTicks * 50L

    fun initialize(state: PosableState) {
        startTime = state.animationSeconds
        endTime = startTime + durationTicks / 20F
        initialized = true
    }

    override fun run(
        context: RenderContext,
        model: PosableModel,
        state: PosableState,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        headYaw: Float,
        headPitch: Float,
        intensity: Float
    ): Boolean {
        if (!initialized) {
            initialize(state)
        }

        val now = state.animationSeconds
        val durationSeconds = (endTime - startTime)
        val passedSeconds = (now - startTime)
        val ratio = min(passedSeconds / durationSeconds, 1F)
        val newIntensity = curve(ratio).coerceIn(0F..1F)
        val oldIntensity = 1 - newIntensity

        model.setDefault()

        model.applyPose(state, beforePose, oldIntensity)
        beforePose.animations.forEach {
            it.apply(context, model, state, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, oldIntensity)
        }

        model.applyPose(state, afterPose, newIntensity)
        afterPose.animations.forEach {
            it.apply(context, model, state, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, newIntensity)
        }

        return ratio < 1F
    }
}