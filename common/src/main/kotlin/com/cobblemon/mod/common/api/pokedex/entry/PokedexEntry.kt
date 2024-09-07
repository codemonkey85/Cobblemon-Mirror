/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex.entry

import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeIdentifier
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

class PokedexEntry(
    val id: ResourceLocation,
    val speciesId: ResourceLocation,
    val displayAspects: MutableSet<String> = mutableSetOf(),
    val conditionAspects: MutableSet<String> = mutableSetOf(),
    val forms: MutableList<PokedexForm> = mutableListOf(),
    val variations: MutableList<PokedexCosmeticVariation> = mutableListOf()
) {
    fun clone() = PokedexEntry(
        id,
        speciesId,
        displayAspects,
        conditionAspects,
        forms.map { it.clone() }.toMutableList(),
        variations.map { it.clone() }.toMutableList()
    )

    fun combinedWith(other: PokedexEntry): PokedexEntry {
        val copy = clone()
        other.forms.forEach { form ->
            if (form.displayForm !in copy.forms.map { it.displayForm }) {
                copy.forms.add(form.clone())
            } else {
                copy.forms.find { it.displayForm == form.displayForm }!!.unlockForms.addAll(form.unlockForms)
            }
        }
        other.variations.forEach { variation ->
            if (variation.displayName !in copy.variations.map { it.displayName }) {
                copy.variations.add(variation.clone())
            }
        }

        return copy
    }

    fun add(addition: DexEntryAdditions.DexEntryAddition) {
        addition.forms.forEach { form ->
            if (form.displayForm !in forms.map { it.displayForm }) {
                forms.add(form)
            } else {
                forms.find { it.displayForm == form.displayForm }!!.unlockForms.addAll(form.unlockForms)
            }
        }
        addition.variations.forEach { variation ->
            if (variation.displayName !in variations.map { it.displayName }) {
                variations.add(variation)
            }
        }
    }

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
            val displayAspects = buffer.readList { buffer.readString() }.toMutableSet()
            val conditionAspects = buffer.readList { buffer.readString() }.toMutableSet()
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
}

class PokedexForm {
    var displayForm: String = "Normal"
    var unlockForms: MutableSet<String> = mutableSetOf()

    fun clone() = PokedexForm().also {
        it.displayForm = displayForm
        it.unlockForms.addAll(unlockForms)
    }
}