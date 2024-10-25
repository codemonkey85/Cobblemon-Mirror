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

/**
 * Provides a generic layer for adapters which are expected to allow for children
 *
 * @author NickImpact
 * @since August 22nd, 2022
 */
abstract class CobblemonAdapterParent<S> : CobblemonAdapter<S> {

    val children: MutableList<CobblemonAdapter<*>> = mutableListOf()
    fun with(vararg children: CobblemonAdapter<*>) : CobblemonAdapter<S> {
        this.children.addAll(children)
        return this
    }

    override fun <E : StorePosition, T : PokemonStore<E>> load(storeClass: Class<T>, uuid: UUID, registryAccess: RegistryAccess): T? {
        return this.provide(storeClass, uuid, registryAccess)
            ?: children.firstNotNullOfOrNull { it.load(storeClass, uuid, registryAccess) }
    }

    abstract fun <E : StorePosition, T : PokemonStore<E>> provide(storeClass: Class<T>, uuid: UUID, registryAccess: RegistryAccess): T?
}