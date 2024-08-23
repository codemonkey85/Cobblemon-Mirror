/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex

import com.bedrockk.molang.runtime.struct.QueryStruct
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addStandardFunctions
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import net.minecraft.resources.ResourceLocation

abstract class AbstractPokedexManager {

    open val speciesRecords: MutableMap<ResourceLocation, SpeciesDexRecord> = mutableMapOf()

    @Transient
    val struct = QueryStruct(hashMapOf()).addStandardFunctions()
        .addFunction("get_species_record") { params ->
            val speciesId = params.getString(0).asIdentifierDefaultingNamespace()
            speciesRecords[speciesId]?.struct ?: QueryStruct(hashMapOf())
        }

    fun deleteSpeciesRecord(speciesId: ResourceLocation) {
        speciesRecords.remove(speciesId)
        markDirty()
    }

    fun getOrCreateSpeciesRecord(speciesId: ResourceLocation): SpeciesDexRecord {
        return speciesRecords.getOrPut(speciesId) {
            val record = SpeciesDexRecord()
            record.initialize(this)
            onSpeciesRecordUpdated(record)
            record
        }
    }

    fun getKnowledgeForSpecies(speciesId: ResourceLocation): PokedexEntryProgress {
        return speciesRecords[speciesId]?.getKnowledge() ?: PokedexEntryProgress.NONE
    }

    open fun onSpeciesRecordUpdated(speciesDexRecord: SpeciesDexRecord) {
        // Save stuff and packet updates
    }

    open fun markDirty() {
        // Save stuff
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