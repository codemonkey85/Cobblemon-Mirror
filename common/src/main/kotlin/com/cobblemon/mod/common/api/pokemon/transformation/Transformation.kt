/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.transformation

import com.cobblemon.mod.common.api.pokemon.transformation.requirement.TransformationRequirement
import com.cobblemon.mod.common.api.pokemon.transformation.trigger.*
import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * Represents a visual and species change in a Pokemon that is triggered by a player-driven or world-driven event.
 *
 * @author Segfault Guy
 * @since Sept 8th, 2023
 */
interface Transformation {

    /** The [TransformationTrigger] to attempt this transformation. */
    val trigger: TransformationTrigger

    /** The [TransformationRequirement]s that need to be satisfied for this transformation. */
    val requirements: Set<TransformationRequirement>

    /** Checks if all [TransformationRequirement]s are met. */
    fun checkRequirements(pokemon: Pokemon) = this.requirements.all { requirement -> requirement.check(pokemon) }

    /**
     * Tests if the given [Pokemon] passes all the conditions and is ready to transform.
     *
     * @param pokemon The [Pokemon] being queried.
     * @param context The optional [TriggerContext] needed to transform.
     * @return Whether the [Transformation] can start.
     */
    fun test(pokemon: Pokemon, context: TriggerContext?) = this.trigger.testTrigger(context) && this.checkRequirements(pokemon)

    /**
     * Starts this transformation if conditions are met.
     *
     * @param pokemon The [Pokemon] being transformed.
     * @param context The optional [TriggerContext] needed to transform.
     * @return Whether the [Transformation] was successful.
     */
    fun start(pokemon: Pokemon, context: TriggerContext?): Boolean {
        if (test(pokemon, context)) {
            forceStart(pokemon)
            return true
        }
        return false
    }

    /**
     * Starts this transformation without evaluating the requirements.
     *
     * @param pokemon The [Pokemon] being transformed.
     */
    fun forceStart(pokemon: Pokemon) { this.requirements.forEach { it.fulfill(pokemon) } }
}
