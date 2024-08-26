/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex.entry

import com.cobblemon.mod.common.api.pokedex.AbstractPokedexManager
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeIdentifier
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

class PokedexEntry(
    val id: ResourceLocation,
    val speciesId: ResourceLocation,
    val displayAspects: Set<String> = emptySet(),
    val conditionAspects: Set<String> = emptySet(),
    val forms: MutableList<PokedexForm> = mutableListOf(),
    val variations: List<PokedexCosmeticVariation>
) {
    fun encode(buf: RegistryFriendlyByteBuf) {
        buf.writeIdentifier(id)
        buf.writeIdentifier(speciesId)
        buf.writeCollection(displayAspects) { _, aspect -> buf.writeString(aspect) }
        buf.writeCollection(conditionAspects) { _, aspect -> buf.writeString(aspect) }
        buf.writeCollection(forms) { _, form ->
            buf.writeString(form.displayForm)
            buf.writeCollection(form.unlockForms) { _, unlockForm -> buf.writeString(unlockForm) }
        }
        buf.writeCollection(variations) { _, variation -> variation.encode(buf) }
    }
    companion object {
        fun decode(buffer: RegistryFriendlyByteBuf): PokedexEntry {
            val id = buffer.readIdentifier()
            val entryId = buffer.readIdentifier()
            val displayAspects = buffer.readList { buffer.readString() }.toSet()
            val conditionAspects = buffer.readList { buffer.readString() }.toSet()
            val forms = buffer.readList {
                val form = PokedexForm()
                form.displayForm = buffer.readString()
                form.unlockForms.addAll(buffer.readList { buffer.readString() })
                form
            }
            val variations = buffer.readList { PokedexCosmeticVariation().also { it.decode(buffer) } }
            return PokedexEntry(id, entryId, displayAspects, conditionAspects, forms, variations)
        }
    }

    fun isVisible(dexData: AbstractPokedexManager): Boolean {
        val speciesRecord = dexData.getSpeciesRecord(speciesId) ?: return false
        return conditionAspects.all(speciesRecord::hasAspect) && speciesRecord.hasAtLeast(PokedexEntryProgress.ENCOUNTERED)
    }
}

class PokedexForm {
    var displayForm: String = "Normal"
    var unlockForms: MutableSet<String> = mutableSetOf()

    fun getKnowledge(speciesId: ResourceLocation, dexData: AbstractPokedexManager) = unlockForms
        .maxOfOrNull { dexData.getSpeciesRecord(speciesId)?.getKnowledge() ?: PokedexEntryProgress.NONE }
        ?: PokedexEntryProgress.NONE
}