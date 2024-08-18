/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex.filters

import com.cobblemon.mod.common.api.pokedex.ClientPokedex
import com.cobblemon.mod.common.api.pokedex.EntryFilter
import com.cobblemon.mod.common.api.pokedex.PokedexJSONRegistry
import com.cobblemon.mod.common.pokedex.DexData
import com.cobblemon.mod.common.pokedex.DexPokemonData

class RegionFilter(clientPokedex: ClientPokedex, val region: DexData) : EntryFilter(clientPokedex) {
    override fun filter(dexPokemonData: DexPokemonData): Boolean {
        if (region.pokemonList.contains(dexPokemonData)) return true
        val containedDexes = region.containedDexes.map { PokedexJSONRegistry.getByIdentifier(it)!! }
        containedDexes.forEach { if (it.pokemonList.contains(dexPokemonData)) return true }
        return false
    }
}