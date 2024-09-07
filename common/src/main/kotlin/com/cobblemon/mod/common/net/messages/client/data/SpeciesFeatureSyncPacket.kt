/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.data

import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatureProvider
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatures
import com.cobblemon.mod.common.api.pokemon.feature.SynchronizedSpeciesFeatureProvider
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Base class for a registry sync packet that synchronizes [SynchronizedSpeciesFeatureProvider]s.
 *
 * @author Hiroku
 * @since November 13th, 2023
 */
abstract class SpeciesFeatureSyncPacket<T : SpeciesFeatureSyncPacket<T>>(
    speciesFeatureProviders: Map<String, SpeciesFeatureProvider<*>>
) : DataRegistrySyncPacket<Map.Entry<String, SynchronizedSpeciesFeatureProvider<*>>, T>(
    speciesFeatureProviders.entries
        .filter { (_, v) -> v is SynchronizedSpeciesFeatureProvider<*> && v.visible}
        .filterIsInstance<Map.Entry<String, SynchronizedSpeciesFeatureProvider<*>>>()
) {
    override fun encodeEntry(
        buffer: RegistryFriendlyByteBuf,
        entry: Map.Entry<String, SynchronizedSpeciesFeatureProvider<*>>
    ) {

        val typeName = SpeciesFeatures.types.entries.find { it.value.isInstance(entry.value) }?.key
        val value = entry.value
        if (typeName == null) {
            buffer.writeBoolean(false)
            return
        } else {
            buffer.writeBoolean(true)
        }
        buffer.writeString(entry.key)
        buffer.writeString(typeName)
        value.saveToBuffer(buffer, toClient = true)
    }

    override fun decodeEntry(buffer: RegistryFriendlyByteBuf): Map.Entry<String, SynchronizedSpeciesFeatureProvider<*>>? {
        if (!buffer.readBoolean()) {
            return null
        }
        val name = buffer.readString()
        val typeName = buffer.readString()
        val typeClass = SpeciesFeatures.types[typeName]
            ?: throw IllegalStateException(
                """
                    A custom species feature provider, $typeName with encoding implementations was registered on the server and 
                    not the client, and therefore cannot be synced. Remove the implementation or install it 
                    on the client.
                """.trimIndent()
            )
        val instance = typeClass.getConstructor().newInstance()
        if (instance !is SynchronizedSpeciesFeatureProvider) {
            throw IllegalStateException("Somehow a non-SynchronizedSpeciesFeatureProvider was sent to the client. Version mismatch?")
        } else {
            instance.loadFromBuffer(buffer)
        }

        return object : Map.Entry<String, SynchronizedSpeciesFeatureProvider<*>> {
            override val key: String = name
            override val value: SynchronizedSpeciesFeatureProvider<*> = instance
        }
    }
}