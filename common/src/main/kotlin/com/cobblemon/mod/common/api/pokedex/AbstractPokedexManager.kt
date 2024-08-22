package com.cobblemon.mod.common.api.pokedex

import net.minecraft.resources.ResourceLocation

abstract class AbstractPokedexManager() {
    abstract val entries: Map<String, String>

    fun getValueForKey(key: String): String? {
        return entries[key]
    }

    fun getKnowledgeForSpecies(speciesId: ResourceLocation): PokedexEntryProgress {
        return getValueForKey(getKnowledgeKeyForSpecies(speciesId))?.let {
            PokedexEntryProgress.valueOf(it)
        } ?: PokedexEntryProgress.NONE
    }

    companion object {
        const val NUM_CAUGHT_KEY = "cobblemon.pokedex.entries.caught"
        const val NUM_SEEN_KEY = "cobblemon.pokedex.entries.seen"

        fun getKeyForSpeciesBase(speciesId: ResourceLocation): String {
            return "cobblemon.pokedex.${speciesId.path}"
        }

        fun getKnowledgeKeyForSpecies(speciesId: ResourceLocation): String {
            return "${getKeyForSpeciesBase(speciesId)}.knowledge"
        }

        fun getKnowledgeKeyForForm(speciesId: ResourceLocation, formName: String): String {
            return "${getKeyForSpeciesBase(speciesId)}.${formName.lowercase()}.knowledge"
        }

        fun getCaptureMethodKeyForSpecies(speciesId: ResourceLocation): String {
            return "${getKeyForSpeciesBase(speciesId)}.capturemethod"
        }
    }
}