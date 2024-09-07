/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.animation.PoseAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import net.minecraft.world.entity.Entity

/**
 * Animation that analyzes a [BedrockAnimation] and applies transformations to the model based on
 * the given animation time.
 *
 * @param animation The [BedrockAnimation] to be played
 *
 * @author landonjw
 * @since January 5th, 2022
 */
class BedrockPoseAnimation(val animation: BedrockAnimation) : PoseAnimation() {
    val particleKeyFrames = animation.effects.filterIsInstance<BedrockParticleKeyframe>()

    init {
        if (animation.animationLength != -1.0 && !animation.shouldLoop) {
            Cobblemon.LOGGER.error("Found animation that is not set to loop and has a set length but is being used in an idle animation. This will cause things like T-posing! ${animation.name}")
        }
    }

    override fun setupAnim(context: RenderContext, model: PosableModel, state: PosableState, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, headYaw: Float, headPitch: Float, intensity: Float) {
        animation.run(context, model, state, state.animationSeconds, limbSwing, limbSwingAmount, ageInTicks, intensity)
    }

    override fun applyEffects(entity: Entity?, state: PosableState, previousSeconds: Float, newSeconds: Float) {
        val effectiveAnimationLength = animation.animationLength.takeUnless { it <= 0 }?.toFloat() ?: animation.effects.maxOfOrNull { it.seconds }?.takeIf { it != 0F }
        val (loopedPreviousSeconds, loopedNewSeconds) = if (effectiveAnimationLength != null) {
            (previousSeconds % effectiveAnimationLength) to (newSeconds % effectiveAnimationLength)
        } else {
            previousSeconds to newSeconds
        }
        animation.applyEffects(entity, state, loopedPreviousSeconds, loopedNewSeconds)
    }
}