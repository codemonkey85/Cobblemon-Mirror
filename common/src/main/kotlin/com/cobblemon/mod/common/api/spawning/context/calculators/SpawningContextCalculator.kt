/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.context.calculators

import com.cobblemon.mod.common.api.PrioritizedList
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.spawning.SpawnCause
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.FluidTags
import net.minecraft.world.level.block.state.BlockState

/**
 * Calculates some kind of [SpawningContext] from a particular type of input data. This
 * is necessary when you create a new type of [SpawningContext] with [SpawningContext.register]
 * to know how to actually create these contexts.
 *
 * If you are adding to the world spawner, then you probably actually want to create an
 * [AreaSpawningContextCalculator].
 *
 * @author Hiroku
 * @since January 31st, 2022
 */
interface SpawningContextCalculator<I : SpawningContextInput, O : SpawningContext> {
    companion object {
        val isAirCondition: (BlockState) -> Boolean = { it.isAir || (!it.isSolid && !it.fluidState.`is`(FluidTags.WATER)) }
        val isSolidCondition: (BlockState) -> Boolean = { it.isSolid }
        val isWaterCondition: (BlockState) -> Boolean = { it.fluidState.`is`(FluidTags.WATER) && it.fluidState.isSource  }
        val isLavaCondition: (BlockState) -> Boolean = { it.fluidState.`is`(FluidTags.LAVA) && it.fluidState.isSource }

        private val calculators = PrioritizedList<SpawningContextCalculator<*, *>>()
        val prioritizedAreaCalculators: List<AreaSpawningContextCalculator<*>>
            get() = calculators.filterIsInstance<AreaSpawningContextCalculator<*>>()

        fun register(calculator: SpawningContextCalculator<*, *>, priority: Priority = Priority.NORMAL) {
            calculators.add(priority, calculator)
        }

        fun unregister(calculator: SpawningContextCalculator<*, *>) {
            calculators.remove(calculator)
        }
    }

    val name: String

    /** Tries creating a [SpawningContext] from the given input. Returning null should be a last resort. */
    fun calculate(input: I): O?
}

/**
 * Base class for input to a [SpawningContextCalculator].
 *
 * @author Hiroku
 * @since January 31st, 2022
 */
open class SpawningContextInput(
    /** What caused the spawn context, as a [SpawnCause]. */
    val cause: SpawnCause,
    /** The [Level] the spawning context exists in. */
    val world: ServerLevel
)