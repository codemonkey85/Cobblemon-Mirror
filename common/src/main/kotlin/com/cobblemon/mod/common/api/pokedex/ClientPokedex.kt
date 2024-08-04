/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex

import com.cobblemon.mod.common.api.pokedex.trackeddata.GlobalTrackedData
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreType
import com.cobblemon.mod.common.api.storage.player.client.ClientInstancedPlayerData
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.net.messages.client.SetClientPlayerDataPacket
import com.cobblemon.mod.common.pokedex.DexPokemonData
import com.cobblemon.mod.common.util.readEnumConstant
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeEnumConstant
import com.cobblemon.mod.common.util.writeIdentifier
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

/**
 * Clientside representation of the Pokedex
 *
 * @author Apion
 * @since February 24, 2024
 */
class ClientPokedex(
    val speciesEntries: MutableMap<ResourceLocation, SpeciesPokedexEntry>,
    val globalTrackedData: MutableSet<GlobalTrackedData>,
) : ClientInstancedPlayerData(false) {

    override fun encode(buf: RegistryFriendlyByteBuf) {
        buf.writeInt(speciesEntries.size)
        speciesEntries.forEach {
            buf.writeIdentifier(it.key)
            encodeSpeciesEntry(buf, it.value)
        }
        buf.writeInt(globalTrackedData.size)
        globalTrackedData.forEach {
            it.encode(buf)
        }
    }

    fun getKnowledgeCount(pokedexEntryProgress: PokedexEntryProgress, dexData: Collection<DexPokemonData>): Int {
        return dexData.count{ discoveryLevel(it.identifier) == pokedexEntryProgress }
    }

    fun discoveryLevel(species: ResourceLocation): PokedexEntryProgress {
        val entry = speciesEntries[species] ?: return PokedexEntryProgress.NONE

        return entry.highestDiscoveryLevel()
    }

    companion object {
        fun encodeSpeciesEntry(buf: RegistryFriendlyByteBuf, speciesEntry: SpeciesPokedexEntry) {
            buf.writeInt(speciesEntry.formEntries.size)
            speciesEntry.formEntries.forEach {
                buf.writeString(it.key)
                encodeFormEntry(buf, it.value)
            }
        }

        fun encodeFormEntry(buf: RegistryFriendlyByteBuf, formEntry: FormPokedexRecords) {
            buf.writeEnumConstant(formEntry.knowledge)
        }

        fun decodeSpeciesEntry(buf: RegistryFriendlyByteBuf): SpeciesPokedexEntry {
            val formEntries = mutableMapOf<String, FormPokedexRecords>()
            val numForms = buf.readInt()
            for (i in 1..numForms) {
                val formStr = buf.readString()
                val formEntry = decodeFormEntry(buf)
                formEntries[formStr] = formEntry
            }
            val result = SpeciesPokedexEntry()
            result.formEntries = formEntries
            return result
        }

        fun decodeFormEntry(buf: RegistryFriendlyByteBuf): FormPokedexRecords {
            val knowledge = buf.readEnumConstant(PokedexEntryProgress::class.java)
            val result = FormPokedexRecords()
            result.knowledge = knowledge
            return FormPokedexRecords(knowledge)
        }

        fun decode(buf: RegistryFriendlyByteBuf): SetClientPlayerDataPacket {
            val speciesMap = mutableMapOf<ResourceLocation, SpeciesPokedexEntry>()
            val statSet = mutableSetOf<GlobalTrackedData>()
            val numSpecies = buf.readInt()
            for (i in 1..numSpecies) {
                val speciesId = buf.readIdentifier()
                val speciesEntry = decodeSpeciesEntry(buf)
                speciesMap[speciesId] = speciesEntry
            }
            val numStats = buf.readInt()
            for (i in 1..numStats) {
                statSet.add(GlobalTrackedData.decode(buf))
            }
            val clientPokedex = ClientPokedex(speciesMap, statSet)
            return SetClientPlayerDataPacket(PlayerInstancedDataStoreType.POKEDEX, clientPokedex)
        }

        fun afterDecodeAction(data: ClientInstancedPlayerData) {
            if (data !is ClientPokedex) return
            CobblemonClient.clientPokedexData = data
        }

        fun afterIncrementalDecodeAction(data: ClientInstancedPlayerData) {
            if (data !is ClientPokedex) return
            data.globalTrackedData.forEach {curData ->
                //This might look weird but the trackedData currently in the set has the same hashCode,
                // but equals() is false
                CobblemonClient.clientPokedexData.globalTrackedData.remove(curData)
                CobblemonClient.clientPokedexData.globalTrackedData.add(curData)
            }
            data.speciesEntries.forEach {
                CobblemonClient.clientPokedexData.speciesEntries[it.key] = it.value
            }

        }
    }

}