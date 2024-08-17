/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokedex

import com.cobblemon.mod.common.api.net.Decodable
import com.cobblemon.mod.common.api.net.Encodable
import com.cobblemon.mod.common.api.pokedex.PokedexEntryCategory
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readEnumConstant
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeEnumConstant
import com.cobblemon.mod.common.util.writeIdentifier
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

class DexPokemonData(
    var identifier : ResourceLocation = cobblemonResource("dex.pokemon"),
    var forms : MutableList<String> = mutableListOf(),
    var category: PokedexEntryCategory = PokedexEntryCategory.STANDARD,
    var tags: MutableList<String> = mutableListOf(),
    var invisibleUntilFound : Boolean = false,
    var visualNumber : String? = null,
    var skipAutoNumbering: Boolean = false
): Decodable, Encodable, Comparable<DexPokemonData> {

    val species : Species?
        get() = PokemonSpecies.getByIdentifier(identifier)

    fun combine(diff: DexPokemonData): DexPokemonData {
        val newForms: MutableList<String> = mutableListOf()
        newForms.addAll(forms)
        newForms.addAll(diff.forms)

        val newTags: MutableList<String> = mutableListOf()
        newTags.addAll(tags)
        newTags.addAll(diff.tags)

        val newInvisibleUntilFound = invisibleUntilFound || diff.invisibleUntilFound
        val newSkipAutoNumbering = skipAutoNumbering || diff.invisibleUntilFound

        val newVisualNumber = if (visualNumber != null) visualNumber else if (diff.visualNumber != null) diff.visualNumber else null

        return DexPokemonData(
            identifier = identifier,
            forms = newForms,
            category = category,
            tags = newTags,
            invisibleUntilFound = newInvisibleUntilFound,
            visualNumber = newVisualNumber,
            skipAutoNumbering = newSkipAutoNumbering
        )
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeIdentifier(identifier)
        buffer.writeInt(forms.size)
        forms.forEach {
            buffer.writeString(it)
        }
        buffer.writeEnumConstant(category)
        buffer.writeInt(tags.size)
        tags.forEach {
            buffer.writeString(it)
        }
        buffer.writeBoolean(invisibleUntilFound)
        buffer.writeNullable(visualNumber) { _, v -> buffer.writeString(v)}
        buffer.writeBoolean(skipAutoNumbering)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        identifier = buffer.readIdentifier()
        val formsOrderListSize = buffer.readInt()
        for(i in 0 until formsOrderListSize){
            forms.add(buffer.readString())
        }
        category = buffer.readEnumConstant(PokedexEntryCategory::class.java)
        val tagListSize = buffer.readInt()
        for(i in 0 until tagListSize){
            tags.add(buffer.readString())
        }
        invisibleUntilFound = buffer.readBoolean()
        visualNumber = buffer.readNullable { buffer.readString() }
        skipAutoNumbering = buffer.readBoolean()
    }

    //Compares using identifiers.
    override fun compareTo(other: DexPokemonData): Int {
        return identifier.compareTo(other.identifier)
    }
}