/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.properties

import com.cobblemon.mod.common.api.properties.CustomPokemonProperty
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * A property that holds a float value.
 *
 * @author Hiroku
 * @since September 15th, 2024
 */
open class FloatProperty(
    val key: String,
    val value: Float,
    private val pokemonApplicator: (pokemon: Pokemon, value: Float) -> Unit,
    private val entityApplicator: (pokemonEntity: PokemonEntity, value: Float) -> Unit,
    private val pokemonMatcher: (pokemon: Pokemon, value: Float) -> Boolean,
    private val entityMatcher: (pokemonEntity: PokemonEntity, value: Float) -> Boolean
) : CustomPokemonProperty {

    override fun asString() = "${this.key}=${this.value}"

    override fun apply(pokemon: Pokemon) {
        this.pokemonApplicator.invoke(pokemon, this.value)
    }

    override fun apply(pokemonEntity: PokemonEntity) {
        this.entityApplicator.invoke(pokemonEntity, this.value)
    }

    override fun matches(pokemon: Pokemon) = this.pokemonMatcher.invoke(pokemon, this.value)
    override fun matches(pokemonEntity: PokemonEntity) = this.entityMatcher.invoke(pokemonEntity, this.value)
}