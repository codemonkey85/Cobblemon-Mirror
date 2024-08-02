/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon

import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * A simple functional interface for extracting a property from a [Pokemon] and putting it into a [PokemonProperties].
 *
 * This mainly exists for the purposes of [Pokemon.createPokemonProperties] specifically.
 *
 * @author Hiroku
 * @since May 12th, 2022
 */
fun interface PokemonPropertyExtractor {
    companion object {
        @JvmField
        val ALL = mutableListOf<PokemonPropertyExtractor>()

        @JvmField
        val SPECIES = add { pokemon, properties -> properties.species = pokemon.species.resourceIdentifier.toString() }
        @JvmField
        val FORM = add { pokemon, properties -> properties.form = pokemon.form.formOnlyShowdownId() }
        @JvmField
        val SHINY = add { pokemon, properties -> properties.shiny = pokemon.shiny }
        @JvmField
        val ASPECTS = add { pokemon, properties -> properties.aspects = pokemon.aspects }
        @JvmField
        val LEVEL = add { pokemon, properties -> properties.level = pokemon.level }
        @JvmField
        val GENDER = add { pokemon, properties -> properties.gender = pokemon.gender }
        @JvmField
        val FRIENDSHIP = add { pokemon, properties -> properties.friendship = pokemon.friendship }
        @JvmField
        val POKEBALL = add { pokemon, properties ->  properties.pokeball = pokemon.caughtBall.name.toString() }
        @JvmField
        val NATURE = add { pokemon, properties ->  properties.nature = pokemon.nature.name.toString() }
        @JvmField
        val ABILITY = add { pokemon, properties ->  properties.ability = pokemon.ability.name }
        @JvmField
        val NICKNAME = add { pokemon, properties -> properties.nickname = pokemon.nickname }
        @JvmField
        val STATUS = add { pokemon, properties -> properties.status = pokemon.status?.status?.showdownName }
        @JvmField
        val IVS = add { pokemon, properties -> properties.ivs = pokemon.ivs }
        @JvmField
        val EVS = add { pokemon, properties -> properties.evs = pokemon.evs }

        @JvmField
        val ILLUSION = mutableListOf(SPECIES, FORM, ASPECTS, GENDER, NICKNAME, SHINY)
        @JvmField
        val TRANSFORM = mutableListOf(SPECIES, FORM, ASPECTS, GENDER)

        fun add(extractor: PokemonPropertyExtractor): PokemonPropertyExtractor {
            ALL.add(extractor)
            return extractor
        }
    }

    operator fun invoke(pokemon: Pokemon, properties: PokemonProperties)
}
