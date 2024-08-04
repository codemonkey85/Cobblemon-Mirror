/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.evolution

import com.cobblemon.mod.common.api.pokemon.evolution.progress.EvolutionProgress
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.evolution.controller.ClientEvolutionController
import com.cobblemon.mod.common.pokemon.evolution.controller.ServerEvolutionController

/**
 * Responsible for holding all available [EvolutionLike]s in the [Pokemon].
 * Also handles all the networking behind them.
 * For the Cobblemon default implementations see [ClientEvolutionController] & [ServerEvolutionController].
 *
 * @author Licious
 * @since April 28th, 2022
 */
interface EvolutionController<T : EvolutionLike, out I : PreProcessor> : MutableSet<T> {

    /**
     * Resolves the [Pokemon] attached to this controller.
     *
     * @return The [Pokemon] this controller is attached to.
     */
    fun pokemon(): Pokemon

    /**
     * Starts the given evolution on this controller.
     * All the necessary networking will be handled.
     *
     * @param evolution The [EvolutionLike] starting.
     */
    fun start(evolution: T)

    /**
     * Returns an immutable collection of [EvolutionProgress]es attached to this controller.
     *
     * @return The currently tracked [EvolutionProgress]es.
     */
    fun progress(): Collection<EvolutionProgress<*>>

    /**
     * Attaches a [EvolutionProgress] to this controller.
     *
     * @param P The type of [EvolutionProgress].
     * @param progress The [EvolutionProgress] being attached for tracking.
     * @return The instance being tracked.
     */
    fun <P : EvolutionProgress<*>> trackProgress(progress: P): P

    /**
     * Attempts to find an [EvolutionProgress] in [progress] or creates and tracks using [trackProgress].
     *
     * @param P The type of [EvolutionProgress].
     * @param predicate The lambda attempting to find specific progress. This needs to ensure the [EvolutionProgress] is of type [P] or the cast will fail.
     * @param progressFactory The lambda creating a new instance of type [P] if [predicate] can't find an element.
     * @return The found match in [progress] or the created and now tracked with [trackProgress].
     */
    fun <P : EvolutionProgress<*>> progressFirstOrCreate(predicate: (progress: EvolutionProgress<*>) -> Boolean, progressFactory: () -> P): P

    fun asIntermediate(): I

}