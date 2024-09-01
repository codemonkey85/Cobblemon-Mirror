/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.feature

import com.cobblemon.mod.common.api.properties.CustomPokemonProperty
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import com.google.gson.JsonObject
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * A species feature value that is a string value.
 *
 * @author Hiroku
 * @since December 30th, 2022
 */
class StringSpeciesFeature(
    override val name: String,
    var value: String
) : SynchronizedSpeciesFeature, CustomPokemonProperty {
    override fun saveToNBT(pokemonNBT: CompoundTag): CompoundTag {
        pokemonNBT.putString(name, value)
        return pokemonNBT
    }

    override fun loadFromNBT(pokemonNBT: CompoundTag): SpeciesFeature {
        value = pokemonNBT.getString(name)?.takeIf { it.isNotBlank() }?.lowercase() ?: return this
        return this
    }

    override fun saveToJSON(pokemonJSON: JsonObject): JsonObject {
        pokemonJSON.addProperty(name, value)
        return pokemonJSON
    }

    override fun loadFromJSON(pokemonJSON: JsonObject): SpeciesFeature {
        value = pokemonJSON.get(name)?.asString?.lowercase() ?: return this
        return this
    }

    override fun saveToBuffer(buffer: RegistryFriendlyByteBuf, toClient: Boolean) {
        buffer.writeString(value)
    }

    override fun loadFromBuffer(buffer: RegistryFriendlyByteBuf) {
        value = buffer.readString()
    }

    override fun asString() = "$name=$value"

    override fun apply(pokemon: Pokemon) {
        val featureProvider = SpeciesFeatures.getFeature(name) ?: return
        if (featureProvider in SpeciesFeatures.getFeaturesFor(pokemon.species)) {
            val existingFeature = pokemon.getFeature<StringSpeciesFeature>(name)
            if (existingFeature != null) {
                existingFeature.value = value
            } else {
                pokemon.features.add(StringSpeciesFeature(name, value))
            }
            pokemon.updateAspects()
        }
    }

    override fun matches(pokemon: Pokemon) = pokemon.getFeature<StringSpeciesFeature>(name)?.value == value
}