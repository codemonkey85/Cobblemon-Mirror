/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex.filter

import com.cobblemon.mod.common.api.pokedex.entry.PokedexEntry
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies

/**
 * A Pokedex [EntryFilter] that filters out entries that do not contain the current search.
 *
 * @author whatsy
 * @since September 4th, 2024
 * @param searchString The string to use when checking.
 */
class SearchFilter(val searchString: String) : EntryFilter() {
    override fun test(entry: PokedexEntry): Boolean {
        if (searchString == "") return true
        val species = PokemonSpecies.getByIdentifier(entry.speciesId) ?: return false

        return species.translatedName.string.contains(searchString.trim(), true)
    }

}