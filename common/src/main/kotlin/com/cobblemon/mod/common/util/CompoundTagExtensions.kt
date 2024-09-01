/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import com.cobblemon.mod.common.CobblemonEntities
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtUtils

fun CompoundTag.isPokemonEntity() : Boolean {
    return this.getString("id").equals(CobblemonEntities.POKEMON_KEY.toString())
}

fun CompoundTag.readBlockPosWithFallback(name: String) : BlockPos {
    val tag = get(name)
    return NbtUtils.readBlockPos(this, name).orElse((tag as? CompoundTag)?.let { BlockPos(it.getInt("X"), it.getInt("Y"), it.getInt("Z")) } ?: BlockPos.ZERO)
}