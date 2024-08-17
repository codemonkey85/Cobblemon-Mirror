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
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.WaveFunction
import net.minecraft.client.model.geom.ModelPart

/**
 * Animation simply works by moving a part's rotation along a particular function
 */
class RotationFunctionPoseAnimation(
    val part: ModelPart,
    val function: WaveFunction,
    val axis: Int,
    val timeVariable: (state: PosableState, limbSwing: Float, ageInTicks: Float) -> Float?,
) : PoseAnimation() {
    override fun setupAnim(context: RenderContext, model: PosableModel, state: PosableState, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, headYaw: Float, headPitch: Float, intensity: Float) {
        part.addRotation(axis, function(timeVariable(state, limbSwing, ageInTicks) ?: 0F) * intensity)
    }
}