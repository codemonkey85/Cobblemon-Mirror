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
    private val dexCalculatedValues = mutableMapOf<ResourceLocation, MutableMap<PokedexValueCalculator<*>, Any>>()
    private val globalCalculatedValues = mutableMapOf<GlobalPokedexValueCalculator<*>, Any>()

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
            record.initialize(this, speciesId)
            onSpeciesRecordUpdated(record)
            record
        }
    }

    fun getKnowledgeForSpecies(speciesId: ResourceLocation): PokedexEntryProgress {
        return speciesRecords[speciesId]?.getKnowledge() ?: PokedexEntryProgress.NONE
    }

    open fun onSpeciesRecordUpdated(speciesDexRecord: SpeciesDexRecord) {
        // Save stuff and packet updates
        dexCalculatedValues.clear()
        globalCalculatedValues.clear()
    }

    fun <T : Any> getDexCalculatedValue(dex: ResourceLocation, calculatedPokedexValue: PokedexValueCalculator<T>): T {
        val existingValue = dexCalculatedValues[dex]?.get(calculatedPokedexValue) as? T
        if (existingValue != null) {
            return existingValue
        } else {
            val vals = dexCalculatedValues.getOrPut(dex) { mutableMapOf() }
            val newValue = calculatedPokedexValue.calculate(this, Dexes.dexEntryMap[dex]!!.getEntries().associateBy { it.id })
            vals[calculatedPokedexValue] = newValue
            return newValue
        }
    }

    fun <T : Any> getGlobalCalculatedValue(calculatedPokedexValue: GlobalPokedexValueCalculator<T>): T {
        val existingValue = globalCalculatedValues[calculatedPokedexValue] as? T
        if (existingValue != null) {
            return existingValue
        } else {
            val newValue = calculatedPokedexValue.calculate(this)
            globalCalculatedValues[calculatedPokedexValue] = newValue
            return newValue
        }
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
            return "${getKnowledgeKeyForSpecies(speciesId)}.${formName.lowercase()}"
        }

        fun getCaptureMethodKeyForSpecies(speciesId: ResourceLocation): String {
            return "${getKeyForSpeciesBase(speciesId)}.capturemethod"
        }
    }
}