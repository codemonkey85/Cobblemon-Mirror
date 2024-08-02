/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.properties

import com.cobblemon.mod.common.api.properties.CustomPokemonProperty
import com.cobblemon.mod.common.api.properties.CustomPokemonPropertyType
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbilityType

/**
 * A type of [CustomPokemonPropertyType] which asserts that the Pokémon's ability
 * must be a hidden ability.
 *
 * @author Hiroku
 * @since November 1st, 2022
 */
object HiddenAbilityPropertyType : CustomPokemonPropertyType<HiddenAbilityProperty> {
    override val keys = setOf("hiddenability", "ha")
    override val needsKey = true
    override fun fromString(value: String?) = HiddenAbilityProperty()
    override fun examples() = emptySet<String>()
}

class HiddenAbilityProperty : CustomPokemonProperty {
    override fun asString() = "hiddenability"
    override fun apply(pokemon: Pokemon) {
        val possible = pokemon.form.abilities.mapping.flatMap { it.value }
            .filter { it.type == HiddenAbilityType }
        val picked = possible.randomOrNull() ?: return
        pokemon.updateAbility(picked.template.create(false))
    }

    override fun matches(pokemon: Pokemon) = pokemon.form.abilities.mapping
        .flatMap { it.value }
        .find { it.template == pokemon.ability.template }
        ?.type == HiddenAbilityType
}