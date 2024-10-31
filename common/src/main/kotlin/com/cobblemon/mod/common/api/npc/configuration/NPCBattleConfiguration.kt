/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.npc.configuration

import com.cobblemon.mod.common.api.npc.NPCPartyProvider
import com.cobblemon.mod.common.util.DataKeys
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.RegistryFriendlyByteBuf

class NPCBattleConfiguration {
    var canChallenge = false
    var party: NPCPartyProvider? = null
    var simultaneousBattles = false
    var healAfterwards = true

    fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeBoolean(canChallenge)
        buffer.writeBoolean(simultaneousBattles)
        buffer.writeBoolean(healAfterwards)
//        buffer.writeNullable(party) { _, provider ->
//            buffer.writeString(provider.type)
//            provider.encode(buffer)
//        }
    }

    fun decode(buffer: RegistryFriendlyByteBuf) {
        canChallenge = buffer.readBoolean()
        simultaneousBattles = buffer.readBoolean()
        healAfterwards = buffer.readBoolean()
//        party = buffer.readNullable {
//            val type = buffer.readString()
//            val providerBuilder = NPCPartyProvider.types[type]
//                ?: throw IllegalArgumentException("Failed to load NPC party provider of type: $type")
//
//            val decodedParty = providerBuilder(type)
//            decodedParty.decode(buffer)
//            decodedParty
//        }
    }

    fun saveToNBT(nbt: CompoundTag) {
        nbt.putBoolean(DataKeys.NPC_CAN_CHALLENGE, canChallenge)
        nbt.putBoolean(DataKeys.NPC_SIMULTANEOUS_BATTLES, simultaneousBattles)
        nbt.putBoolean(DataKeys.NPC_HEAL_AFTERWARDS, healAfterwards)
//        val party = party
//        if (party != null) {
//            val partyNBT = CompoundTag()
//            partyNBT.putString(DataKeys.NPC_PARTY_TYPE, party.type)
//            party.saveToNBT(partyNBT)
//            nbt.put(DataKeys.NPC_PARTY, partyNBT)
//        }
    }

    fun loadFromNBT(nbt: CompoundTag) {
        canChallenge = nbt.getBoolean(DataKeys.NPC_CAN_CHALLENGE)
        simultaneousBattles = nbt.getBoolean(DataKeys.NPC_SIMULTANEOUS_BATTLES)
        healAfterwards = nbt.getBoolean(DataKeys.NPC_HEAL_AFTERWARDS)
//        val partyNBT = nbt.getCompound(DataKeys.NPC_PARTY)
//        if (!partyNBT.isEmpty) {
//            val type = partyNBT.getString(DataKeys.NPC_PARTY_TYPE)
//            val providerBuilder = NPCPartyProvider.types[type]
//                ?: throw IllegalArgumentException("Failed to load NPC party provider of type: $type")
//            party = providerBuilder(type).also { it.loadFromNBT(partyNBT) }
//        }
    }
}