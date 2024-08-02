/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.evolution

import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.evolution.variants.LevelUpEvolution

/**
 * Represents an evolution of a [Pokemon] that can occur without any additional context or actions.
 * For the default implementation see [LevelUpEvolution].
 *
 * @author Licious
 * @since March 19th, 2022
 */
interface PassiveEvolution : Evolution {

    /**
     * Checks if the given [Pokemon] satisfies the requirements.
     * If yes the evolution will attempt to start.
     *
     * @param pokemon The [Pokemon] being tested.
     * @return If the [Pokemon] will evolve.
     */
    fun attemptEvolution(pokemon: Pokemon): Boolean {
        if (super.test(pokemon)) {
            return super.evolve(pokemon)
        }
        return false
    }

    /**
     * If the evolution will disappear once the conditions are no longer met.
     */
    val permanent: Boolean


}