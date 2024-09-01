/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.data

import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatureAssignments
import com.cobblemon.mod.common.util.*
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

/**
 * A registry sync packet for the [SpeciesFeatureAssignments] registry.
 *
 * @author Hiroku
 * @since November 13th, 2023
 */
class SpeciesFeatureAssignmentSyncPacket(
    data: Map<ResourceLocation, MutableSet<String>>
) : DataRegistrySyncPacket<Map.Entry<ResourceLocation, MutableSet<String>>, SpeciesFeatureAssignmentSyncPacket>(data.entries) {
    override val id = ID
    override fun decodeEntry(buffer: RegistryFriendlyByteBuf): Map.Entry<ResourceLocation, MutableSet<String>> {
        val key = buffer.readIdentifier()
        val assignments = buffer.readList { buffer.readString() }.toMutableSet()
        return object : Map.Entry<ResourceLocation, MutableSet<String>> {
            override val key = key
            override val value = assignments
        }
    }

    override fun encodeEntry(buffer: RegistryFriendlyByteBuf, entry: Map.Entry<ResourceLocation, MutableSet<String>>) {
        buffer.writeIdentifier(entry.key)
        buffer.writeCollection(entry.value) { _, value -> buffer.writeString(value) }
    }

    override fun synchronizeDecoded(entries: Collection<Map.Entry<ResourceLocation, MutableSet<String>>>) {
        SpeciesFeatureAssignments.loadOnClient(entries.associate { it.toPair() })
    }

    companion object {
        val ID = cobblemonResource("species_feature_assignment_sync")
        fun decode(buffer: RegistryFriendlyByteBuf) = SpeciesFeatureAssignmentSyncPacket(emptyMap()).apply { decodeBuffer(buffer) }
    }
}