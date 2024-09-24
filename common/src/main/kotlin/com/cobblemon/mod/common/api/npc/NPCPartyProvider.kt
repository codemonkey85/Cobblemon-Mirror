/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.npc

import com.cobblemon.mod.common.api.npc.partyproviders.DynamicPartyProvider
import com.cobblemon.mod.common.api.npc.partyproviders.NPCParty
import com.cobblemon.mod.common.api.npc.partyproviders.SimplePartyProvider
import com.cobblemon.mod.common.api.storage.party.PartyStore
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.google.gson.JsonElement
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.server.level.ServerPlayer

/**
 * A provider of a party for battling the NPC. Completely custom party providers will only display
 * as text labels in any GUIs.
 *
 * @author Hiroku
 * @since August 16th, 2023
 */
interface NPCPartyProvider {
    companion object {
        val types = mutableMapOf<String, (String) -> NPCPartyProvider>(
            SimplePartyProvider.TYPE to { SimplePartyProvider() },
            DynamicPartyProvider.TYPE to { DynamicPartyProvider() }
        )
    }

    val type: String
    fun provide(npc: NPCEntity, level: Int): NPCParty
    fun loadFromJSON(json: JsonElement)
}