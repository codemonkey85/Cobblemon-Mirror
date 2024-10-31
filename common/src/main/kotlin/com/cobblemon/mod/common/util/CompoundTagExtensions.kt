/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import com.cobblemon.mod.common.CobblemonEntities
import net.minecraft.nbt.CompoundTag

fun CompoundTag.isPokemonEntity() : Boolean {
    return this.getString("id").equals(CobblemonEntities.POKEMON_KEY.toString())
}