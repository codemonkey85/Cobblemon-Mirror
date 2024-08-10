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
import com.cobblemon.mod.common.api.pokemon.evolution.ContextEvolution
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirement
import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * Represents a [ContextEvolution] with [Pokemon] context.
 * This is triggered by trading.
 * The context is the received [Pokemon] from the trade.
 *
 * @property requiredContext The [PokemonProperties] representation of the expected received [Pokemon] from the trade.
 * @author Licious
 * @since March 20th, 2022
 */
open class TradeEvolution(
    override val id: String,
    override val result: PokemonProperties,
    override val shedder: PokemonProperties?,
    override val requiredContext: PokemonProperties,
    override var optional: Boolean,
    override var consumeHeldItem: Boolean,
    override val requirements: MutableSet<EvolutionRequirement>,
    override val learnableMoves: MutableSet<MoveTemplate>
) : ContextEvolution<Pokemon, PokemonProperties> {
    constructor(): this(
        id = "id",
        result = PokemonProperties(),
        shedder = null,
        requiredContext = PokemonProperties(),
        optional = true,
        consumeHeldItem = true,
        requirements = mutableSetOf(),
        learnableMoves = mutableSetOf()
    )

    override fun testContext(pokemon: Pokemon, context: Pokemon) = this.requiredContext.matches(context)

    override fun equals(other: Any?) = other is TradeEvolution && other.id.equals(this.id, true)

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + ADAPTER_VARIANT.hashCode()
        return result
    }

    companion object {
        const val ADAPTER_VARIANT = "trade"
    }
}