/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage

import net.minecraft.nbt.CompoundTag

data class StoreCoordinates<T : StorePosition>(
    val store: PokemonStore<T>,
    val position: T
) {
    fun saveToNBT(nbt: CompoundTag) {
        store.savePositionToNBT(position, nbt)
    }

    fun get() = store[position]
    fun remove() = store.remove(position)
}