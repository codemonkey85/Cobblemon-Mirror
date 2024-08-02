/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.config.starter

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.config.Category
import com.cobblemon.mod.common.config.LastChangedVersion
import com.cobblemon.mod.common.config.NodeCategory
import com.cobblemon.mod.common.util.adapters.pokemonPropertiesShortAdapter
import com.google.gson.GsonBuilder

class StarterConfig {
    companion object {
        val GSON = GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .registerTypeAdapter(PokemonProperties::class.java, pokemonPropertiesShortAdapter)
            .create()
    }

    @NodeCategory(Category.Starter)
    var allowStarterOnJoin = true

    @NodeCategory(Category.Starter)
    @LastChangedVersion("1.5.0")
    var promptStarterOnceOnly = true

    @NodeCategory(Category.Starter)
    var starters = mutableListOf(
        StarterCategory(
            name = "Kanto",
            displayName = "cobblemon.starterselection.category.kanto",
            pokemon = mutableListOf(
                PokemonProperties.parse("Bulbasaur level=10"),
                PokemonProperties.parse("Charmander level=10"),
                PokemonProperties.parse("Squirtle level=10"),
            )
        ),
        StarterCategory(
            name = "Johto",
            displayName = "cobblemon.starterselection.category.johto",
            pokemon = mutableListOf(
                PokemonProperties.parse("Chikorita level=10"),
                PokemonProperties.parse("Cyndaquil level=10"),
                PokemonProperties.parse("Totodile level=10"),
            )
        ),
        StarterCategory(
            name = "Hoenn",
            displayName = "cobblemon.starterselection.category.hoenn",
            pokemon = mutableListOf(
                    PokemonProperties.parse("Treecko level=10"),
                    PokemonProperties.parse("Torchic level=10"),
                    PokemonProperties.parse("Mudkip level=10"),
            )
        ),
        StarterCategory(
            name = "Sinnoh",
            displayName = "cobblemon.starterselection.category.sinnoh",
            pokemon = mutableListOf(
                PokemonProperties.parse("Turtwig level=10"),
                PokemonProperties.parse("Chimchar level=10"),
                PokemonProperties.parse("Piplup level=10"),
            )
        ),
        StarterCategory(
            name = "Unova",
            displayName = "cobblemon.starterselection.category.unova",
            pokemon = mutableListOf(
                PokemonProperties.parse("Snivy level=10"),
                PokemonProperties.parse("Tepig level=10"),
                PokemonProperties.parse("Oshawott level=10"),
            )
        ),
        StarterCategory(
            name = "Kalos",
            displayName = "cobblemon.starterselection.category.kalos",
            pokemon = mutableListOf(
                PokemonProperties.parse("Chespin level=10"),
                PokemonProperties.parse("Fennekin level=10"),
                PokemonProperties.parse("Froakie level=10"),
            )
        ),
        StarterCategory(
            name = "Alola",
            displayName = "cobblemon.starterselection.category.alola",
            pokemon = mutableListOf(
                PokemonProperties.parse("Rowlet level=10"),
                PokemonProperties.parse("Litten level=10"),
                PokemonProperties.parse("Popplio level=10"),
            )
        ),
        StarterCategory(
            name = "Galar",
            displayName = "cobblemon.starterselection.category.galar",
            pokemon = mutableListOf(
                PokemonProperties.parse("Grookey level=10"),
                PokemonProperties.parse("Scorbunny level=10"),
                PokemonProperties.parse("Sobble level=10"),
            )
        ),
        StarterCategory(
            name = "Hisui Bias",
            displayName = "cobblemon.starterselection.category.hisui_bias",
            pokemon = mutableListOf(
                PokemonProperties.parse("Rowlet region_bias=hisui level=10 pokeball=ancient_poke_ball"),
                PokemonProperties.parse("Cyndaquil region_bias=hisui level=10 pokeball=ancient_poke_ball"),
                PokemonProperties.parse("Oshawott region_bias=hisui level=10 pokeball=ancient_poke_ball"),
            )
        ),
        StarterCategory(
            name = "Paldea",
            displayName = "cobblemon.starterselection.category.paldea",
            pokemon = mutableListOf(
                PokemonProperties.parse("Sprigatito level=10"),
                PokemonProperties.parse("Fuecoco level=10"),
                PokemonProperties.parse("Quaxly level=10"),
            )
        )
    )
}