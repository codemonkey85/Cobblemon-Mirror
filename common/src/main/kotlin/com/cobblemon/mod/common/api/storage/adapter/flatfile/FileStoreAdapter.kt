/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.adapter.flatfile

import com.cobblemon.mod.common.api.storage.PokemonStore
import com.cobblemon.mod.common.api.storage.StorePosition
import com.cobblemon.mod.common.api.storage.adapter.CobblemonAdapter
import java.util.UUID
import net.minecraft.core.RegistryAccess

/**
 * Interface for some type of file backend for [PokemonStore] saving and loading.
 *
 * @author Hiroku
 * @since November 29th, 2021
 */
interface FileStoreAdapter<S> : CobblemonAdapter<S> {
    /** Converts the specified store into a serialized form. This is expected to run on the server thread, and as fast as possible. */
    fun <E : StorePosition, T : PokemonStore<E>> serialize(store: T, registryAccess: RegistryAccess): S
    /** Writes the serialized form of a store into the appropriate file. This should be threadsafe. */
    fun save(storeClass: Class<out PokemonStore<*>>, uuid: UUID, serialized: S)
}