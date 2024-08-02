/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.feature

import com.cobblemon.mod.common.pokemon.Pokemon
import com.google.gson.JsonObject
import net.minecraft.nbt.CompoundTag

/**
 * A provider of [SpeciesFeature]s. These must be registered via [SpeciesFeatures] either programmatically or
 * using the datapack structure. Note that for datapacks to be able to use an implementation of this interface,
 * it must be registered in [SpeciesFeatures.types] and then the JSON must include a 'type' field that references
 * the key used for that mapping.
 *
 * @author Hiroku
 * @since November 29th, 2022
 */
interface SpeciesFeatureProvider<T : SpeciesFeature> {
    /**
     * Attempts to create a species feature for the given [Pokemon]. It is appropriate to check for an existing feature
     * that is compatible with this type and transfer / retain that feature instead of constructing a new one. For example
     * a feature for an Ekan's snake pattern should be retained when evolving into Arbok, and species feature invocation
     * occurs whenever a species changes.
     */
    operator fun invoke(pokemon: Pokemon): T?
    operator fun invoke(nbt: CompoundTag): T?
    operator fun invoke(json: JsonObject): T?
}