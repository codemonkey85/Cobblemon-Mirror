/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.mojang.serialization.Codec

class IVs : PokemonStats() {
    override val acceptableRange = 0..MAX_VALUE
    override val defaultValue = 0
    // TODO: Hyper training

    companion object {
        const val MAX_VALUE = 31

        @JvmStatic
        fun createRandomIVs(minPerfectIVs : Int = 0) : IVs = Cobblemon.statProvider.createEmptyIVs(minPerfectIVs)

        @JvmStatic
        val CODEC: Codec<IVs> = Codec.unboundedMap(Stat.PERMANENT_ONLY_CODEC, Codec.intRange(0, MAX_VALUE))
            .xmap(
                { map ->
                    val ivs = Cobblemon.statProvider.createEmptyIVs(0)
                    map.forEach { (stat, value) -> ivs[stat] = value }
                    return@xmap ivs
                },
                { ivs ->
                    val map = hashMapOf<Stat, Int>()
                    ivs.forEach { (stat, value) -> map[stat] = value }
                    return@xmap map
                }
            )
    }
}