/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.npc.partyproviders

import com.cobblemon.mod.common.api.npc.NPCPartyProvider
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.storage.party.NPCPartyStore
import com.cobblemon.mod.common.api.storage.party.PartyStore
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.*
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.server.level.ServerPlayer

class SimplePartyProvider : NPCPartyProvider {
    companion object {
        const val TYPE = "simple"
    }

    @Transient
    override val type = TYPE

    val pokemon = mutableListOf<PokemonProperties>()

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeSizedInt(IntSize.U_BYTE, pokemon.size)
        for (pokemon in this.pokemon) {
            buffer.writeString(pokemon.originalString)
        }
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        repeat(times = buffer.readSizedInt(IntSize.U_BYTE)) {
            pokemon.add(PokemonProperties.parse(buffer.readString()))
        }
    }

    override fun saveToNBT(nbt: CompoundTag) {
        for ((index, pokemon) in this.pokemon.withIndex()) {
            nbt.putString(DataKeys.NPC_PARTY_POKEMON + index, pokemon.originalString)
        }
    }

    override fun loadFromNBT(nbt: CompoundTag) {
        var index = 0
        while (nbt.contains(DataKeys.NPC_PARTY_POKEMON + index)) {
            this.pokemon.add(PokemonProperties.parse(nbt.getString(DataKeys.POKEMON_PROPERTIES + index)))
            index++
        }
    }

    override fun loadFromJSON(json: JsonElement) {
        json as JsonObject
        if (json.has(DataKeys.NPC_PARTY_POKEMON.lowercase())) {
            json.get(DataKeys.NPC_PARTY_POKEMON.lowercase()).asJsonArray.forEach { pokemon.add(PokemonProperties.parse(it.asString)) }
        }
    }

    override fun provide(npc: NPCEntity, challengers: List<ServerPlayer>): PartyStore {
        return NPCPartyStore(npc).apply {
            for (properties in pokemon) {
                add(properties.create())
            }
        }
    }
}