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
import net.minecraft.entity.Entity

/**
 * Represents an animation that has no expiry and can run forever. It's essentially a looping animation or one
 * for which there is no duration. As of 1.6 this is very murky, we could probably combine this with Stateful.
 *
 * @author Hiroku
 * @since December 4th, 2021
 */
abstract class StatelessAnimation {
    open var labels: Set<String> = setOf()
    open var condition: (PosableState) -> Boolean = { true }

    open fun withCondition(condition: (PosableState) -> Boolean): StatelessAnimation {
        this.condition = condition
        return this
    }

    open fun withCondition(condition: ExpressionLike): StatelessAnimation {
        this.condition = { state -> state.runtime.resolveBoolean(condition) }
        return this
    }

    protected abstract fun setAngles(
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
        setAngles(context, model, state, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, intensity)
    }

    open fun applyEffects(entity: Entity, state: PosableState, previousSeconds: Float, newSeconds: Float) {}
}