/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.feature

import com.cobblemon.mod.common.api.pokemon.aspect.AspectProvider
import com.google.gson.JsonObject
import net.minecraft.nbt.CompoundTag

/**
 * A piece of state that can be added to some species of Pokémon. Registering an implementing class
 * using [SpeciesFeatures.register] adds it as a usable value in the [SpeciesFeatures] list. All Pokémon
 * are given the opportunity to be assigned features depending on whether it's a global feature or they
 * have opted into it in their species JSON.
 *
 * The role of this is to allow species-specific data to be attached to individual Pokémon, such as an alolan
 * flag or a Vivillon pattern variety. This is powerful when combined with [AspectProvider]s.
 *
 * @author Hiroku
 * @since May 13th, 2022
 */
interface SpeciesFeature {
    val name: String
    fun saveToNBT(pokemonNBT: CompoundTag): CompoundTag
    fun loadFromNBT(pokemonNBT: CompoundTag): SpeciesFeature
    fun saveToJSON(pokemonJSON: JsonObject): JsonObject
    fun loadFromJSON(pokemonJSON: JsonObject): SpeciesFeature
}