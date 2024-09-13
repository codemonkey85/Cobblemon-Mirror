/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.evolution.requirement

import com.cobblemon.mod.common.api.pokemon.evolution.ContextEvolution
import com.cobblemon.mod.common.api.pokemon.evolution.Evolution
import com.cobblemon.mod.common.pokemon.Pokemon
import com.mojang.serialization.Codec

/**
 * Represents a requirement for an evolution.
 * Requirements are constant and will never change regardless of the backing action.
 * For dynamic requirements see [ContextEvolution].
 *
 * See [Evolution.requirements] & [Evolution.test] for usage.
 *
 * @author Licious
 * @since March 19th, 2022
 */
interface EvolutionRequirement {

    /**
     * The associated [EvolutionRequirementType].
     */
    val type: EvolutionRequirementType<*>

    /**
     * Checks if the given [Pokemon] satisfies the requirement.
     *
     * @param pokemon The [Pokemon] being queried.
     * @return If the requirement was satisfied.
     */
    fun check(pokemon: Pokemon): Boolean

    companion object {

        @JvmStatic
        val CODEC: Codec<EvolutionRequirement> = EvolutionRequirementType.REGISTRY
            .byNameCodec()
            .dispatch(EvolutionRequirement::type) { it.codec() }

    }

}