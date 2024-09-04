package com.cobblemon.mod.common.api.pokedex.filter

import com.cobblemon.mod.common.api.pokedex.entry.PokedexEntry

abstract class EntryFilter {

    abstract fun test(entry: PokedexEntry): Boolean

}