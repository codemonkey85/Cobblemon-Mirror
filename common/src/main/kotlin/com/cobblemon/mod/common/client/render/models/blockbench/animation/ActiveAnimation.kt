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
import net.minecraft.world.entity.Entity

/**
 * An animation that requires some kind of state specific to an instance of [PosableState].
 * This differs from [PoseAnimation]s in that that variety uses a single shared
 * instance of the animation for the model, even when there may be a hundred entities using it.
 *
 * @author Hiroku
 * @since December 5th, 2021
 */
interface ActiveAnimation {
    /** Whether or not this animation is being used as a transition and therefore should prevent other pose transitions from occurring. */
    val isTransition: Boolean
    /** The animation's duration in seconds. */
    val duration: Float
    /** Whether a primary animation will not remove this animation before playing. */
    val enduresPrimaryAnimations: Boolean

    fun start(state: PosableState) {}

    /** Runs the animation. Returns true if the animation should continue. */
    fun run(
        context: RenderContext,
        model: PosableModel,
        state: PosableState,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        headYaw: Float,
        headPitch: Float,
        intensity: Float
    ): Boolean

    /** Applies animation effects, such as particle effects. These can occur on tick, therefore not necessarily on screen. */
    fun applyEffects(entity: Entity?, state: PosableState, previousSeconds: Float, newSeconds: Float) {}
}