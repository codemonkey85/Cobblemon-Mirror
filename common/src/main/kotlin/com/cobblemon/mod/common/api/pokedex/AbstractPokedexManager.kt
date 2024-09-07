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
import com.cobblemon.mod.common.api.pokedex.entry.PokedexEntry
import com.cobblemon.mod.common.api.pokedex.entry.PokedexForm
import com.cobblemon.mod.common.pokemon.Gender
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

    fun getSpeciesRecord(speciesId: ResourceLocation): SpeciesDexRecord? {
        return speciesRecords[speciesId]
    }

    fun getOrCreateSpeciesRecord(speciesId: ResourceLocation): SpeciesDexRecord {
        return speciesRecords.getOrPut(speciesId) {
            val record = SpeciesDexRecord()
            record.initialize(this, speciesId)
            onSpeciesRecordUpdated(record)
            record
        }
    }

    fun getHighestKnowledgeFor(entry: PokedexEntry): PokedexEntryProgress {
        val speciesRecord = getSpeciesRecord(entry.speciesId) ?: return PokedexEntryProgress.NONE
        val hasAllAspects = entry.conditionAspects.all(speciesRecord::hasAspect)
        if (!hasAllAspects) {
            return PokedexEntryProgress.NONE
        }
        // For each distinct form in this entry, get the highest knowledge level for the unlock forms, then take the highest of those.
        return entry.forms.maxOfOrNull { form ->
            form.unlockForms.maxOfOrNull {
                speciesRecord.getFormRecord(it)?.knowledge ?: PokedexEntryProgress.NONE
            } ?: PokedexEntryProgress.NONE
        } ?: PokedexEntryProgress.NONE
    }

    fun getEncounteredForms(entry: PokedexEntry) : List<PokedexForm> {
        return getFormsWithKnowledge(entry, PokedexEntryProgress.ENCOUNTERED)
    }

    fun getCaughtForms(entry: PokedexEntry) : List<PokedexForm> {
        return getFormsWithKnowledge(entry, PokedexEntryProgress.CAUGHT)
    }

    fun getFormsWithKnowledge(entry: PokedexEntry, knowledge: PokedexEntryProgress) : List<PokedexForm> {
        val speciesRecord = getSpeciesRecord(entry.speciesId) ?: return emptyList()
        val hasAllAspects = entry.conditionAspects.all(speciesRecord::hasAspect)
        if (!hasAllAspects) {
            return emptyList()
        }
        // For each distinct form in this entry, get the highest knowledge level for the unlock forms, then take the highest of those.
        return entry.forms.filter { form ->
            form.unlockForms.any { (speciesRecord.getFormRecord(it)?.knowledge ?: PokedexEntryProgress.NONE) >= knowledge }
        }
    }

    fun getSeenShinyStates(entry: PokedexEntry, form: PokedexForm): Set<String> {
        return getSpeciesRecord(entry.speciesId)?.getFormRecord(form.displayForm)?.getSeenShinyStates() ?: return emptySet()
    }

    fun getSeenGenders(entry: PokedexEntry, form: PokedexForm): Set<Gender> {
        return getSpeciesRecord(entry.speciesId)?.getFormRecord(form.displayForm)?.getGenders() ?: emptySet()
    }

    fun getSeenAspects(entry: PokedexEntry): Set<String> {
        return getSpeciesRecord(entry.speciesId)?.getAspects() ?: emptySet()
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