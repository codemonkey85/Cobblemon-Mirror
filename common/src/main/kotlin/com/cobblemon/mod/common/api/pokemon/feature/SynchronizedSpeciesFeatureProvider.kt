/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.feature

import com.cobblemon.mod.common.api.serialization.BufferSerializer
import com.cobblemon.mod.common.client.gui.summary.featurerenderers.SummarySpeciesFeatureRenderer
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * A species feature provider that will be synchronized to the client. For it to be renderable in the summary
 * screen of a Pokémon it must also provide a [SummarySpeciesFeatureRenderer] in [getRenderer]. Note that you
 * really don't want to initialize a renderer outside of that function call because if the server ever initializes
 * it then you're going to get crashes on dedicated servers (this is a bad thing).
 *
 * @author Hiroku
 * @since November 13th, 2023
 */
interface SynchronizedSpeciesFeatureProvider<T : SynchronizedSpeciesFeature> : SpeciesFeatureProvider<T>, BufferSerializer {
    var visible: Boolean
    operator fun invoke(buffer: RegistryFriendlyByteBuf, name: String): T?
    /** Gets the feature from this Pokémon, if it has been created yet. */
    fun get(pokemon: Pokemon): T?
    /** Only run this from the client. */
    fun getRenderer(pokemon: Pokemon): SummarySpeciesFeatureRenderer<T>?
}