/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import net.minecraft.nbt.CompoundTag
import java.util.*

object CompoundTagUtilities {
    @JvmStatic
    fun getPokemonID(nbt: CompoundTag): UUID {
        return nbt.getCompound(DataKeys.POKEMON)
            .getUUID(DataKeys.POKEMON_UUID)
    }

    @JvmStatic
    fun isShoulderPokemon(nbt: CompoundTag): Boolean {
        return nbt.isPokemonEntity()
    }
}