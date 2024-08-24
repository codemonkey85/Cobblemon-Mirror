/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex

import com.bedrockk.molang.runtime.struct.QueryStruct
import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addStandardFunctions
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import com.google.common.collect.Sets
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.ListCodec
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.UUID
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

/**
 * Dex recorded information about a particular Pokémon species. This includes all forms and their information.
 * Across all forms there is a single list of known aspects which is generally used to track cosmetic variation
 * though technically things like gender and form-motivating aspects will get collected inadvertently.
 *
 * @author Hiroku
 * @since August 23rd, 2024
 */
class SpeciesDexRecord {
    companion object {
        val CODEC: Codec<SpeciesDexRecord> = RecordCodecBuilder.create { instance ->
            instance.group(
                ListCodec(PrimitiveCodec.STRING, 0, 512).fieldOf("aspects").forGetter { it.aspects.toList() },
                Codec.unboundedMap(Codec.STRING, FormDexRecord.CODEC).fieldOf("formRecords").forGetter { it.formRecords }
            ).apply(instance) { aspects, formRecords ->
                SpeciesDexRecord().also {
                    it.aspects.addAll(aspects)
                    it.formRecords.putAll(formRecords)
                }
            }
        }
    }

    @Transient
    lateinit var id: ResourceLocation
    private val aspects: MutableSet<String> = mutableSetOf()
    private val formRecords: MutableMap<String, FormDexRecord> = mutableMapOf()

    @Transient
    val struct = QueryStruct(hashMapOf()).addStandardFunctions()
        .addFunction("get_form_record") { params ->
            val formName = params.getString(0)
            formRecords[formName]?.struct ?: QueryStruct(hashMapOf())
        }
        .addFunction("get_or_create_form_record") { params ->
            getOrCreateFormRecord(params.getString(0)).struct
        }
        .addFunction("add_aspect") { params ->
            val aspect = params.getString(0)
            aspects.add(aspect)
        }
        .addFunction("has_knowledge") { params ->
            val knowledge = params.getString(0)
            DoubleValue(formRecords.values.any { it.knowledge.name.equals(knowledge.toString(), ignoreCase = true) })
        }
        // more, also worth moving to something like MoLangFunctions.kt so it's easier to API in more values

    @Transient
    lateinit var pokedexManager: AbstractPokedexManager

    fun initialize(pokedexManager: AbstractPokedexManager, id: ResourceLocation) {
        this.id = id
        this.pokedexManager = pokedexManager
    }

    fun onFormRecordUpdated(formDexRecord: FormDexRecord) {
        pokedexManager.onSpeciesRecordUpdated(this)
    }

    fun addInformation(pokemon: Pokemon, knowledge: PokedexEntryProgress) = aspects.addAll(pokemon.aspects)

    /** Returns true if the given Pokémon contains new information. Internal because it's only to be called from [FormDexRecord.wouldBeDifferent]. */
    internal fun wouldBeDifferent(pokemon: Pokemon) = pokemon.aspects.any { it !in aspects }

    fun getOrCreateFormRecord(formName: String): FormDexRecord {
        return formRecords.getOrPut(formName) {
            val record = FormDexRecord()
            record.initialize(this)
            onFormRecordUpdated(record)
            // Some more stuff eventually
            record
        }
    }

    fun getFormRecord(formName: String): FormDexRecord? {
        return formRecords[formName]
    }

    fun deleteFormRecord(formName: String) {
        formRecords.remove(formName)
    }

    fun getAspects(): Set<String> = this.aspects
    fun hasAspect(aspect: String) = aspect in aspects
    fun getKnowledge() = formRecords.values.maxOfOrNull { it.knowledge } ?: PokedexEntryProgress.NONE

    fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeCollection(aspects) { _, it -> buffer.writeString(it) }
        buffer.writeInt(formRecords.size)
        for ((formName, formRecord) in formRecords) {
            buffer.writeString(formName)
            formRecord.encode(buffer)
        }
    }

    fun decode(buffer: RegistryFriendlyByteBuf) {
        aspects.clear()
        aspects.addAll(buffer.readCollection(Sets::newHashSetWithExpectedSize) { buffer.readString() })
        formRecords.clear()
        val numForms = buffer.readInt()
        for (i in 0 until numForms) {
            val formName = buffer.readString()
            val formRecord = FormDexRecord()
            formRecord.decode(buffer)
            formRecords[formName] = formRecord
        }
    }
}