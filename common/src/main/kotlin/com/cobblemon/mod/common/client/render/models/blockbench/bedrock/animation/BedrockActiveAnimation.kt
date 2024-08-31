/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation

import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.animation.ActiveAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import net.minecraft.world.entity.Entity

/**
 * An active animation that runs a [BedrockAnimation]. It is completed when the underlying
 * bedrock animation is complete.
 *
 * @author Hiroku
 * @since July 16th, 2022
 */
open class BedrockActiveAnimation(
    val animation: BedrockAnimation,
) : ActiveAnimation {
    var startedSeconds = -1F
    var isTransformAnimation = false
    override val duration = animation.animationLength.toFloat()
    private var afterAction: (RenderContext, PosableState) -> Unit = { _, _ -> }

    override val isTransition: Boolean
        get() = isTransformAnimation

    fun andThen(action: (context: RenderContext, PosableState) -> Unit) = this.also {
        it.afterAction = action
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
        if (startedSeconds == -1F) {
            startedSeconds = state.animationSeconds
        }

        return animation.run(context, model, state, state.animationSeconds - startedSeconds, limbSwing, limbSwingAmount, ageInTicks, intensity).also {
            if (!it) {
                afterAction(context, state)
            }
        }
    }

    override fun applyEffects(entity: Entity, state: PosableState, previousSeconds: Float, newSeconds: Float) {
        if (startedSeconds == -1F) {
            startedSeconds = state.animationSeconds
        }
        val previousSecondsOffset = previousSeconds - startedSeconds
        val currentSecondsOffset = newSeconds - startedSeconds
        animation.applyEffects(entity, state, previousSecondsOffset, currentSecondsOffset)
    }
}