package com.cobblemon.mod.common.api.pokedex

import com.cobblemon.mod.common.api.pokedex.entry.PokedexEntry
import net.minecraft.resources.ResourceLocation

interface PokedexValueCalculator<T> {
    fun calculate(dexManager: AbstractPokedexManager, dex: Map<ResourceLocation, PokedexEntry>): T
}

interface GlobalPokedexValueCalculator<T> {
    fun calculate(dexManager: AbstractPokedexManager): T
}

object CaughtCount : PokedexValueCalculator<Int> {
    override fun calculate(dexManager: AbstractPokedexManager, dex: Map<ResourceLocation, PokedexEntry>): Int {
        return dex.entries.map { it.value }.count { dexManager.getKnowledgeForSpecies(it.entryId) == PokedexEntryProgress.CAUGHT }
    }
}

object SeenCount : PokedexValueCalculator<Int> {
    override fun calculate(dexManager: AbstractPokedexManager, dex: Map<ResourceLocation, PokedexEntry>): Int {
        return dex.entries.map { it.value }.count { dexManager.getKnowledgeForSpecies(it.entryId) != PokedexEntryProgress.NONE }
    }
}