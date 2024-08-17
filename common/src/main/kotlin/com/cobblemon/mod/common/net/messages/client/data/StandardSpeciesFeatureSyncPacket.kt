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
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

/**
 * Syncs a species feature provider that was registered under [SpeciesFeatures].
 *
 * @author Hiroku
 * @since November 13th, 2023
 */
class StandardSpeciesFeatureSyncPacket(providers: Map<String, SpeciesFeatureProvider<*>>) : SpeciesFeatureSyncPacket<StandardSpeciesFeatureSyncPacket>(providers) {
    override val id: ResourceLocation = ID
    override fun synchronizeDecoded(entries: Collection<Map.Entry<String, SynchronizedSpeciesFeatureProvider<*>>>) = SpeciesFeatures.loadOnClient(entries)
    companion object {
        val ID = cobblemonResource("standard_species_feature_sync")
        fun decode(buffer: RegistryFriendlyByteBuf) = StandardSpeciesFeatureSyncPacket(emptyMap()).apply { decodeBuffer(buffer) }
    }
}