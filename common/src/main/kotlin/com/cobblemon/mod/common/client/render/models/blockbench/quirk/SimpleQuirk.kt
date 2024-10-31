/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.quirk

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.animation.ActiveAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.animation.PrimaryAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.util.math.random

/**
 * An easy-to-understand [ModelQuirk]. Has a condition, has a loop range, has a time range for how long between
 * occurrences, and has a list of animations to apply. Follows those instructions.
 *
 * @author Hiroku
 * @since September 30th, 2022
 */
class SimpleQuirk(
    private val secondsBetweenOccurrences: Pair<Float, Float>,
    val condition: (context: PosableState) -> Boolean = { true },
    val loopTimes: IntRange = 1..1,
    val animations: (state: PosableState) -> Iterable<ActiveAnimation>
) : ModelQuirk<SimpleQuirkData>() {
    override fun createData(): SimpleQuirkData = SimpleQuirkData()
    override fun apply(context: RenderContext, state: PosableState, data: SimpleQuirkData) {
        if (data.animations.isNotEmpty() || data.primaryAnimation != null) {
            return
        }

        if (!condition(state)) {
            return
        }

        if (data.remainingLoops > 0) {
            applyAnimations(state, data)
            data.remainingLoops--
        }

        if (data.remainingLoops == 0) {
            if (data.nextOccurrenceSeconds > 0F) {
                // Is it time?
                if (data.nextOccurrenceSeconds <= state.animationSeconds) {
                    data.remainingLoops = loopTimes.random() - 1
                    applyAnimations(state, data)
                    data.nextOccurrenceSeconds = -1F
                }
            } else {
                data.nextOccurrenceSeconds = state.animationSeconds + secondsBetweenOccurrences.random()
            }
        }
    }

    private fun applyAnimations(state: PosableState, data: SimpleQuirkData) {
        val (primary, stateful) = animations(state).partition { it is PrimaryAnimation }
        data.animations.addAll(stateful)
        if (primary.isNotEmpty()) {
            val primaryAnimation = primary.first() as PrimaryAnimation
            data.primaryAnimation = primaryAnimation
            state.addPrimaryAnimation(primaryAnimation)
        }
    }
}