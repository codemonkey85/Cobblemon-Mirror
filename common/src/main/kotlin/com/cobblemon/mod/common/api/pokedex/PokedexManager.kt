/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex

import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.storage.player.InstancedPlayerData
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreTypes
import com.cobblemon.mod.common.api.storage.player.client.ClientPokedexManager
import com.cobblemon.mod.common.net.messages.client.SetClientPlayerDataPacket
import com.cobblemon.mod.common.pokedex.scanner.PokedexEntityData
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.getPlayer
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
        val formName = pokemon.form.name
        getOrCreateSpeciesRecord(speciesId).getOrCreateFormRecord(formName).encountered(pokemon)
    }

    fun encounter(pokedexEntityData: PokedexEntityData) {
        val speciesId = pokedexEntityData.species.resourceIdentifier
        val formName = pokedexEntityData.form.name
        getOrCreateSpeciesRecord(speciesId).getOrCreateFormRecord(formName).encountered(pokedexEntityData)
    }

    fun catch(pokemon: Pokemon) {
        val speciesId = pokemon.species.resourceIdentifier
        val formName = pokemon.form.name
        getOrCreateSpeciesRecord(speciesId).getOrCreateFormRecord(formName).caught(pokemon)
    }

    override fun markDirty() {
    }

    override fun initialize() {
        speciesRecords.entries.forEach { (key, value) -> value.initialize(this, key) }
    }

    override fun onSpeciesRecordUpdated(speciesDexRecord: SpeciesDexRecord) {
        super.onSpeciesRecordUpdated(speciesDexRecord)
        uuid.getPlayer()?.sendPacket(
            SetClientPlayerDataPacket(
                type = PlayerInstancedDataStoreTypes.POKEDEX,
                playerData = ClientPokedexManager(mutableMapOf(speciesDexRecord.id to speciesDexRecord)),
                isIncremental = true
            )
        )
    }

    companion object {
        val CODEC = RecordCodecBuilder.create<PokedexManager> { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("uuid").forGetter { it.uuid.toString() },
                Codec.unboundedMap(ResourceLocation.CODEC, SpeciesDexRecord.CODEC).fieldOf("speciesRecords").forGetter { it.speciesRecords }
            ).apply(instance) { uuid, map ->
                //Codec stuff seems to deserialize to an immutable map, so we have to convert it to mutable explicitly
                PokedexManager(UUID.fromString(uuid), map.toMutableMap()).also { it.initialize() }
            }
        }
    }

    override fun toClientData() = ClientPokedexManager(speciesRecords)
}