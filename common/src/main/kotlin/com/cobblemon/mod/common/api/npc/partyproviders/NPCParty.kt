/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.npc.partyproviders

import com.cobblemon.mod.common.api.storage.party.NPCPartyStore
import com.cobblemon.mod.common.api.storage.party.PartyStore
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.writeSizedInt
import net.minecraft.core.RegistryAccess
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.server.level.ServerPlayer

/**
 * Represents a party of Pokemon that an NPC can have. This isn't directly a party of Pokémon so that
 * a dynamically generated party can exist where a party is generated at the time of a challenge.
 *
 * This interface is sealed. Implementations should be either [StaticNPCParty] or [DynamicNPCParty].
 * If you want a custom type then you should implement [DynamicNPCParty] and register it in the
 * [DynamicNPCParty.types] map.
 *
 * @author Hiroku
 * @since July 12th, 2024
 */
sealed interface NPCParty {
    fun getParty(player: ServerPlayer, npc: NPCEntity): PartyStore?
    fun encode(buffer: RegistryFriendlyByteBuf)
    fun decode(buffer: RegistryFriendlyByteBuf)
    fun saveToNBT(nbt: CompoundTag, registryAccess: RegistryAccess)
    fun loadFromNBT(nbt: CompoundTag, registryAccess: RegistryAccess)
}

/**
 * A simple wrapping around a [NPCPartyStore]. This is a straightforward party that is always the same.
 *
 * @author Hiroku
 * @since July 12th, 2024
 */
class StaticNPCParty(val party: NPCPartyStore) : NPCParty {
    override fun getParty(player: ServerPlayer, npc: NPCEntity) = party

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeSizedInt(IntSize.U_BYTE, party.size())
        for (pokemon in party) {
            Pokemon.S2C_CODEC.encode(buffer, pokemon)
        }
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        repeat(times = buffer.readSizedInt(IntSize.U_BYTE)) {
            party.add(Pokemon.S2C_CODEC.decode(buffer))
        }
    }

    override fun saveToNBT(nbt: CompoundTag, registryAccess: RegistryAccess) {
        for ((index, pokemon) in party.withIndex()) {
            nbt.put(DataKeys.NPC_PARTY_POKEMON + index, pokemon.saveToNBT(registryAccess))
        }
    }

    override fun loadFromNBT(nbt: CompoundTag, registryAccess: RegistryAccess) {
        var index = 0
        while (nbt.contains(DataKeys.NPC_PARTY_POKEMON + index)) {
            this.party.add(Pokemon().loadFromNBT(registryAccess, nbt.getCompound(DataKeys.NPC_PARTY_POKEMON + index)))
            index++
        }
    }
}

/**
 * A dynamic party that is generated at the time of a challenge.
 *
 * @author Hiroku
 * @since July 12th, 2024
 */
interface DynamicNPCParty : NPCParty {
    companion object {
        val types = mutableMapOf<String, Class<out NPCParty>>()
        init {}
    }
}