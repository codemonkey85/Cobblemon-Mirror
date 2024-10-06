/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.animation

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.util.Mth

/**
 * Emulation of Minecraft's punch animation.
 *
 * @author Hiroku
 * @since August 5th, 2024
 */
class PunchAnimation(
    val head: ModelPart,
    val body: ModelPart,
    val leftArm: ModelPart,
    val rightArm: ModelPart,
    val swingRight: Boolean = true
): ActiveAnimation {
    override val duration = 0.5F
    var startedTime = -1F
    override val isTransition = false
    override val enduresPrimaryAnimations = false

    override fun start(state: PosableState) {
        super.start(state)
        startedTime = state.animationSeconds
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
        val attackTime = state.animationSeconds - startedTime
        var g: Float = attackTime
        this.body.yRot = Mth.sin(Mth.sqrt(g) * (Math.PI * 2).toFloat()) * 0.2f
        if (!swingRight) {
            this.body.yRot *= -1.0f
        }

        this.rightArm.z = Mth.sin(this.body.yRot) * 5.0f
        this.rightArm.x = -Mth.cos(this.body.yRot) * 5.0f
        this.leftArm.z = -Mth.sin(this.body.yRot) * 5.0f
        this.leftArm.x = Mth.cos(this.body.yRot) * 5.0f
        this.rightArm.yRot += this.body.yRot
        this.leftArm.yRot += this.body.yRot
        this.leftArm.xRot += this.body.yRot
        g = 1.0f - attackTime
        g *= g
        g *= g
        g = 1.0f - g
        val h = Mth.sin(g * Math.PI.toFloat())
        val i: Float = Mth.sin(attackTime * Math.PI.toFloat()) * -(this.head.xRot - 0.7f) * 0.75f
        val modelPart = if (swingRight) this.rightArm else this.leftArm
        modelPart.xRot -= h * 1.2f + i
        modelPart.yRot += this.body.yRot * 2.0f
        modelPart.zRot += Mth.sin(attackTime * Math.PI.toFloat()) * -0.4f
        return attackTime < duration
    }
}