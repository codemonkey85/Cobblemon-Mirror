/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex

import com.cobblemon.mod.common.api.storage.player.InstancedPlayerData
import com.cobblemon.mod.common.api.storage.player.client.ClientPokedexManager
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.Species
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.UUID
import net.minecraft.resources.ResourceLocation

class PokedexManager(
    override val uuid: UUID,
    override val speciesRecords: MutableMap<ResourceLocation, SpeciesDexRecord>
) : AbstractPokedexManager(), InstancedPlayerData {

    fun encounter(pokemon: Pokemon) {
        val speciesId = pokemon.species.resourceIdentifier
        val formName = pokemon.form.formOnlyShowdownId()
        getOrCreateSpeciesRecord(speciesId).getOrCreateFormRecord(formName).encountered(pokemon)
    }

    fun catch(pokemon: Pokemon) {
        val speciesId = pokemon.species.resourceIdentifier
        val formName = pokemon.form.formOnlyShowdownId()
        getOrCreateSpeciesRecord(speciesId).getOrCreateFormRecord(formName).caught(pokemon)
    }

    companion object {
        val CODEC = RecordCodecBuilder.create<PokedexManager> { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("uuid").forGetter { it.uuid.toString() },
                Codec.unboundedMap(ResourceLocation.CODEC, SpeciesDexRecord.CODEC).fieldOf("speciesRecords").forGetter { it.speciesRecords }
            ).apply(instance) { uuid, map ->
                //Codec stuff seems to deserialize to an immutable map, so we have to convert it to mutable explicitly
                PokedexManager(UUID.fromString(uuid), map.toMutableMap())
            }
        }
    }

    override fun toClientData() = ClientPokedexManager(speciesRecords, false)
}