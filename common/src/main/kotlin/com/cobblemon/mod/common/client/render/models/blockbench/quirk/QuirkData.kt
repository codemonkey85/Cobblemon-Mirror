/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.quirk

import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.animation.ActiveAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.PrimaryAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext

/**
 * Simple information about [ModelQuirk]s that might be relevant to how it runs.
 *
 * @author Hiroku
 * @since September 30th, 2022
 */
open class QuirkData {
    /** All of the animations that have been started and are currently in effect due to this quirk. */
    val animations = mutableListOf<ActiveAnimation>()
    /** The primary animation spawned by this quirk, if relevant. */
    var primaryAnimation: PrimaryAnimation? = null

    /** Runs any active quirk behaviour. Called from [ModelQuirk]. */
    open fun run(context: RenderContext, model: PosableModel, state: PosableState, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, headYaw: Float, headPitch: Float, intensity: Float) {
        if (primaryAnimation != null && state.primaryAnimation != primaryAnimation) {
            primaryAnimation = null
        }
        animations.removeIf { !it.run(context, model, state, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, intensity) }
    }
}