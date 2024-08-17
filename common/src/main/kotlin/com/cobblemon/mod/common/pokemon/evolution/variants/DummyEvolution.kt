/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.variants

import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.evolution.Evolution
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirement
import com.cobblemon.mod.common.pokemon.Pokemon

// Used only to comply with pokemon update packet structure
internal class DummyEvolution : Evolution {

    override val id = "dummy"
    override val result: PokemonProperties = PokemonProperties()
    override val shedder: PokemonProperties? = null
    override var optional = false
    override var consumeHeldItem = false
    override val requirements: MutableSet<EvolutionRequirement> = mutableSetOf()
    override val learnableMoves: MutableSet<MoveTemplate> = mutableSetOf()

    override fun test(pokemon: Pokemon) = false

    override fun evolve(pokemon: Pokemon) = false

}