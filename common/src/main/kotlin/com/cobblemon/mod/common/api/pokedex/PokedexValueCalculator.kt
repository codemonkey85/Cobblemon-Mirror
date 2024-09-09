/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex

import com.cobblemon.mod.common.api.pokedex.entry.PokedexEntry
import net.minecraft.resources.ResourceLocation

interface PokedexValueCalculator<T> {
    fun calculate(dexManager: AbstractPokedexManager, dex: Map<ResourceLocation, PokedexEntry>): T
}

interface GlobalPokedexValueCalculator<T> {
    fun calculate(dexManager: AbstractPokedexManager): T
}

object CaughtCount : PokedexValueCalculator<Int>, GlobalPokedexValueCalculator<Int> {
    override fun calculate(dexManager: AbstractPokedexManager): Int {
        return dexManager.speciesRecords.values.count { it.getKnowledge() == PokedexEntryProgress.CAUGHT }
    }

    override fun calculate(dexManager: AbstractPokedexManager, dex: Map<ResourceLocation, PokedexEntry>): Int {
        return dex.entries.map { it.value }.count { dexManager.getKnowledgeForSpecies(it.speciesId) == PokedexEntryProgress.CAUGHT }
    }
}

object SeenCount : PokedexValueCalculator<Int>, GlobalPokedexValueCalculator<Int> {
    override fun calculate(dexManager: AbstractPokedexManager): Int {
        return dexManager.speciesRecords.values.count { it.getKnowledge() != PokedexEntryProgress.NONE }
    }

    override fun calculate(dexManager: AbstractPokedexManager, dex: Map<ResourceLocation, PokedexEntry>): Int {
        return dex.entries.map { it.value }.count { dexManager.getKnowledgeForSpecies(it.speciesId) != PokedexEntryProgress.NONE }
    }
}