/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.requirements.template

import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirement
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.world.entity.LivingEntity

/**
 * An [EvolutionRequirement] that expects a [LivingEntity] to be attached to the [Pokemon].
 * It can be the [PokemonEntity] if present or the [LivingEntity] that owns it.
 *
 * Whenever an entity can't be resolved [EvolutionRequirement.check] will never succeed.
 *
 * @author Licious
 * @since March 21st, 2022
 */
interface EntityQueryRequirement : EvolutionRequirement {
    override fun check(pokemon: Pokemon): Boolean {
        val queriedEntity = pokemon.entity ?: pokemon.getOwnerPlayer() ?: return false
        return this.check(pokemon, queriedEntity)
    }

    /**
     * Checks if the given [Pokemon] & [LivingEntity] satisfies the requirement.
     *
     * @param pokemon The [Pokemon] being queried.
     * @param queriedEntity The [LivingEntity] that can be compared in context of a [Level].
     * @return If the requirement was satisfied.
     */
    fun check(pokemon: Pokemon, queriedEntity: LivingEntity): Boolean
}