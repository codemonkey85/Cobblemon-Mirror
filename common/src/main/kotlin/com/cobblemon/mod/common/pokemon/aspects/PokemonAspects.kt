/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.aspects

import com.cobblemon.mod.common.CobblemonCosmeticItems
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.aspect.AspectProvider
import com.cobblemon.mod.common.api.pokemon.aspect.SingleConditionalAspectProvider
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.server
import net.minecraft.core.registries.Registries

val SHINY_ASPECT = object : SingleConditionalAspectProvider {
    override val aspect = "shiny"
    override fun meetsCondition(pokemon: Pokemon) = pokemon.shiny
    override fun meetsCondition(pokemonProperties: PokemonProperties) = pokemonProperties.shiny == true
}

val GENDER_ASPECT = object : AspectProvider {
    fun getAspectsForGender(gender: Gender) = setOf(
        when (gender) {
            Gender.MALE -> "male"
            Gender.FEMALE -> "female"
            Gender.GENDERLESS -> "genderless"
        }
    )

    override fun provide(pokemon: Pokemon) = getAspectsForGender(pokemon.gender)
    override fun provide(properties: PokemonProperties) = properties.gender?.let { getAspectsForGender(it) } ?: emptySet()
}

val COSMETIC_SLOT_ASPECT = object : AspectProvider {
    override fun provide(pokemon: Pokemon): Set<String> {
        val server = server() ?: return emptySet()
        val cosmeticItems = CobblemonCosmeticItems.cosmeticItems
            .filter { it.pokemon.any { it.matches(pokemon) } }
            .flatMap { it.cosmeticItems }
            .firstOrNull { it.consumedItem.fits(pokemon.cosmeticItem.item, server.registryAccess().registryOrThrow(Registries.ITEM)) }
        return cosmeticItems?.aspects?.toSet() ?: emptySet()
    }

    override fun provide(properties: PokemonProperties) = emptySet<String>()
}