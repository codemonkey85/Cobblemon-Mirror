/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.animation

import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.util.resolveBoolean
import net.minecraft.world.entity.Entity

/**
 * Represents an animation that is shared across every instance of a model. This means that
 * a pose animation cannot store any instance variables that relate to a particular entity
 * that is playing the animation, as this instance is used for all of them.
 *
 * @author Hiroku
 * @since December 4th, 2021
 */
abstract class PoseAnimation {
    open var labels: Set<String> = setOf()
    open var condition: (PosableState) -> Boolean = { true }

    open fun withCondition(condition: (PosableState) -> Boolean): PoseAnimation {
        this.condition = condition
        return this
    }

    open fun withCondition(condition: ExpressionLike): PoseAnimation {
        this.condition = { state -> state.runtime.resolveBoolean(condition) }
        return this
    }

    protected abstract fun setupAnim(
        context: RenderContext,
        model: PosableModel,
        state: PosableState,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        headYaw: Float,
        headPitch: Float,
        intensity: Float
    )

    fun apply(
        context: RenderContext,
        model: PosableModel,
        state: PosableState,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        headYaw: Float,
        headPitch: Float,
        intensity: Float
    ) {
        if (!condition(state)) return
        setupAnim(context, model, state, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, intensity)
    }

    open fun applyEffects(entity: Entity?, state: PosableState, previousSeconds: Float, newSeconds: Float) {}
}