/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon

import com.cobblemon.mod.common.api.berry.Flavor
import com.cobblemon.mod.common.api.pokemon.Natures
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.util.codec.CodecUtils
import com.mojang.serialization.Codec
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth.floor

class Nature(
    val name: ResourceLocation,
    val displayName: String,
    val increasedStat: Stat?,
    val decreasedStat: Stat?,
    val favoriteFlavor: Flavor?,
    val dislikedFlavor: Flavor?
) {
    fun modifyStat(stat: Stat, value: Int): Int {
        return when (stat) {
            increasedStat -> floor(value * 1.1)
            decreasedStat -> floor(value * 0.9)
            else -> value
        }
    }

    companion object {
        @JvmStatic
        val BY_IDENTIFIER_CODEC: Codec<Nature> = CodecUtils.createByIdentifierCodec(
            Natures::getNature,
            Nature::name
        ) { identifier -> "No nature for ID $identifier" }
    }
}