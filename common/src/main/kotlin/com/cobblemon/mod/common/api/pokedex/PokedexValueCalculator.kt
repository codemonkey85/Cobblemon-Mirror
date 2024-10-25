/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex

import com.cobblemon.mod.common.api.pokedex.entry.DexEntries
import com.cobblemon.mod.common.api.pokedex.entry.PokedexEntry
import net.minecraft.resources.ResourceLocation

/**
 * Interface that serves for various calculations can be done around Pokédex. The output of these gets cached and invalidated when the Pokédex of a user updates.
 * As opposed to the [GlobalPokedexValueCalculator] this interface is always called on a specific dex definition (e.g. galar, kanto)
 */
interface PokedexValueCalculator<T> {
    fun calculate(dexManager: AbstractPokedexManager, dex: Map<ResourceLocation, PokedexEntry>): T
}

/**
 * Interface that serves for various calculations can be done around Pokédex. The output of these gets cached and invalidated when the Pokédex of a user updates.
 * The calculation of this interface is aimed to consider all available Pokédex entries and is not limited or filtered to a particular dex
 */
interface GlobalPokedexValueCalculator<T> {
    fun calculate(dexManager: AbstractPokedexManager): T
}

/**
 * Calculates the amount of caught Pokémon (globally or per dex)
 */
object CaughtCount : PokedexValueCalculator<Int>, GlobalPokedexValueCalculator<Int> {
    override fun calculate(dexManager: AbstractPokedexManager): Int {
        return dexManager.speciesRecords.values.count { it.getKnowledge() == PokedexEntryProgress.CAUGHT }
    }

    override fun calculate(dexManager: AbstractPokedexManager, dex: Map<ResourceLocation, PokedexEntry>): Int {
        return dex.entries.map { it.value }.count { dexManager.getKnowledgeForSpecies(it.speciesId) == PokedexEntryProgress.CAUGHT }
    }
}

/**
 * Calculates the amount of seen Pokémon (globally or per dex)
 */
object SeenCount : PokedexValueCalculator<Int>, GlobalPokedexValueCalculator<Int> {
    override fun calculate(dexManager: AbstractPokedexManager): Int {
        return dexManager.speciesRecords.values.count { it.getKnowledge() != PokedexEntryProgress.NONE }
    }

    override fun calculate(dexManager: AbstractPokedexManager, dex: Map<ResourceLocation, PokedexEntry>): Int {
        return dex.entries.map { it.value }.count { dexManager.getKnowledgeForSpecies(it.speciesId) != PokedexEntryProgress.NONE }
    }
}

/**
 * Calculates the seen percentage globally or relative to a particular dex
 */
object SeenPercent : PokedexValueCalculator<Float>, GlobalPokedexValueCalculator<Float> {
    override fun calculate(dexManager: AbstractPokedexManager): Float {
        return dexManager.speciesRecords.values.count { it.getKnowledge() != PokedexEntryProgress.NONE }.toFloat() / DexEntries.entries.count().toFloat() * 100F
    }

    override fun calculate(dexManager: AbstractPokedexManager, dex: Map<ResourceLocation, PokedexEntry>): Float {
        return dex.entries.map { it.value }.count { dexManager.getKnowledgeForSpecies(it.speciesId) != PokedexEntryProgress.NONE }.toFloat() / DexEntries.entries.count().toFloat() * 100F
    }
}

/**
 * Calculates the caught percentage globally or relative to a particular dex
 */
object CaughtPercent : PokedexValueCalculator<Float>, GlobalPokedexValueCalculator<Float> {
    override fun calculate(dexManager: AbstractPokedexManager): Float {
        return dexManager.speciesRecords.values.count { it.getKnowledge() == PokedexEntryProgress.CAUGHT }.toFloat() / DexEntries.entries.count().toFloat().toFloat() * 100F
    }

    override fun calculate(dexManager: AbstractPokedexManager, dex: Map<ResourceLocation, PokedexEntry>): Float {
        return dex.entries.map { it.value }.count { dexManager.getKnowledgeForSpecies(it.speciesId) == PokedexEntryProgress.CAUGHT }.toFloat() / DexEntries.entries.count().toFloat().toFloat() * 100F
    }
}