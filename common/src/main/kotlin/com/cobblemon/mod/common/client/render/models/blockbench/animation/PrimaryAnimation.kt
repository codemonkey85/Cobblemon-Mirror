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
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.WaveFunction
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.WaveFunctions
import java.util.function.Consumer

/**
 * An animation that plays to the exclusion of other primary animations. This can control
 * whether other animations are blocked or faded in or out. It is able to prevent some idle
 * animations based on labels.
 *
 * Fainting, pose transitions, and attack animations are examples of primary animations. They
 * are mandated as being strictly one at a time.
 *
 * @author Hiroku
 * @since November 20th, 2023
 */
class PrimaryAnimation(
    val animation: ActiveAnimation,
    /**
     * The transition curve.
     *
     * When a primary animation is in effect, the pose animations and the primary animation will continue running
     * at the same time but with different 'intensities'. The intensity is a value between 0 and 1 that represents
     * how much of the animation's planned motion will be applied to the bone.
     *
     * This curve parameter is a function that takes a time value between 0 and 1 such that it begins at 0 and reaches
     * 1 at the moment that the primary animation is complete. The function returns a value between 0 and 1 that will
     * be used as the intensity of the primary animation. One minus this intensity will be used as the pose animation's
     * intensity. In other words, the pose animation intensity and the primary animation intensity will always sum to
     * 1 and this curve function represents the intensity of the primary animation.
     *
     * The default value for this curve makes the primary animation rapidly transition in, but does not transition out.
     * The different pre-made options are defined in [WaveFunctions].
     */
    var curve: WaveFunction = { t ->
          if (t < 0.1) {
              t * 10
          } else if (t < 0.9) {
              1F
          } else {
              1F
          }
    },
    /** Labels that can exist on a pose animation that will let it play even when this primary animation is in effect. */
    val excludedLabels: Set<String> = emptySet(),
    override var isTransition: Boolean = false
): ActiveAnimation {
    var started = -1F
    override val duration: Float = animation.duration
    /** Action to occur after the animation is done. */
    var afterAction: Consumer<Unit> = Consumer { }
    override val enduresPrimaryAnimations = false

    override fun start(state: PosableState) {
        super.start(state)
        started = state.animationSeconds
        animation.start(state)
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
        return animation.run(context, model, state, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, intensity)
    }

    /**
     * Whether this animation will attempt to block the motion of the given pose animation. If true, the current
     * intensity of the primary animation will be used to determine how much of the pose animation is blocked.
     *
     * If this primary animation does not block the given pose animation, the pose animation will play at full intensity
     * in parallel to the primary animation. This is mainly used for look animations.
     */
    fun prevents(poseAnimation: PoseAnimation) = poseAnimation.labels.intersect(excludedLabels).isEmpty() && "all" !in excludedLabels
}