/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.datafixer.fix

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatures
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.asResource
import com.cobblemon.mod.common.util.codec.internal.ClientPokemonP1.Companion.FEATURES
import com.cobblemon.mod.common.util.codec.internal.ClientPokemonP1.Companion.FEATURE_ID
import com.google.gson.JsonElement
import com.mojang.datafixers.schemas.Schema
import com.mojang.serialization.Codec
import com.mojang.serialization.Dynamic
import com.mojang.serialization.JsonOps
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag

class FeatureFix(output: Schema) : PokemonFix(output) {
    override fun fixPokemonData(dynamic: Dynamic<*>): Dynamic<*> {
        var features = dynamic.get(FEATURES).asListOpt<CompoundTag> { it.decode<CompoundTag>(CompoundTag.CODEC).result().get().first }
        if (features.result().isPresent) {
            return dynamic //already has Features nbt format... no work needed
        }
        var featureTag = emptyList<CompoundTag>()
        if (dynamic.value is CompoundTag) {
            var rootTag = dynamic.value as CompoundTag
            var species = PokemonSpecies.getByIdentifier(rootTag.getString(DataKeys.POKEMON_SPECIES_IDENTIFIER).asResource())
            SpeciesFeatures.getFeaturesFor(species!!).forEach {
                val feature = it(rootTag) ?: return@forEach
                var tag = CompoundTag()
                tag.putString(FEATURE_ID, feature.name)
                feature.saveToNBT(tag)
                featureTag += tag
            }
            if(featureTag.isEmpty()){
                return dynamic // no features to migrate
            }
            rootTag.put(FEATURES, Codec.list<CompoundTag>(CompoundTag.CODEC)
                .encode<Tag>(featureTag, NbtOps.INSTANCE, NbtOps.INSTANCE.empty()).result().get())
        } else if (dynamic.value is JsonElement) {
            var rootTag = (dynamic.value as JsonElement).asJsonObject
            var species = PokemonSpecies.getByIdentifier(rootTag.get(DataKeys.POKEMON_SPECIES_IDENTIFIER).asString.asResource())
            SpeciesFeatures.getFeaturesFor(species!!).forEach {
                val feature = it(rootTag) ?: return@forEach
                var tag = CompoundTag()
                tag.putString(FEATURE_ID, feature.name)
                feature.saveToNBT(tag)
                featureTag += tag
            }
            if(featureTag.isEmpty()){
                return dynamic // no features to migrate
            }
            rootTag.add(FEATURES, Codec.list<CompoundTag>(CompoundTag.CODEC)
                .encode<JsonElement>(featureTag, JsonOps.COMPRESSED, JsonOps.COMPRESSED.empty()).result().get())
        }
        return dynamic
    }
}