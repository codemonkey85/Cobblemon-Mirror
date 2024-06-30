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
import com.cobblemon.mod.common.client.render.models.blockbench.addPosition
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.WaveFunction
import net.minecraft.client.model.ModelPart

/**
 * Animation simply works by moving a part along a particular function
 */
class TranslationFunctionPoseAnimation(
    val part: ModelPart,
    val function: WaveFunction,
    val axis: Int,
    val timeVariable: (state: PosableState, limbSwing: Float, ageInTicks: Float) -> Float?
) : PoseAnimation() {
    override fun setAngles(context: RenderContext, model: PosableModel, state: PosableState, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, headYaw: Float, headPitch: Float, intensity: Float) {
        part.addPosition(axis, function(timeVariable(state, limbSwing, ageInTicks) ?: 0F) * intensity)
    }
}