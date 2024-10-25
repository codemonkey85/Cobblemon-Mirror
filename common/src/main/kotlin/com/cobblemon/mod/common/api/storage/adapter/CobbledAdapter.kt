/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.adapter

import com.cobblemon.mod.common.api.storage.PokemonStore
import com.cobblemon.mod.common.api.storage.StorePosition
import java.util.UUID
import net.minecraft.core.RegistryAccess

interface CobblemonAdapter<S> {

    /**
     * Attempts to load a store using the specified class and UUID. This would return null if
     * the file does not exist or if this store adapter doesn't know how to load this storage class.
     */
    fun <E : StorePosition, T : PokemonStore<E>> load(storeClass: Class<T>, uuid: UUID, registryAccess: RegistryAccess): T?

}
