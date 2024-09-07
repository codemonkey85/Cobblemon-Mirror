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