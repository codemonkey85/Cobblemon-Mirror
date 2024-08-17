/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.berry

import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.block.BerryBlock
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.minecraft.core.Holder
import net.minecraft.world.level.biome.Biome

object BerryHelper {
    private val CACHE_LOADER = object : CacheLoader<Holder<Biome>, List<BerryBlock>>() {
        override fun load(key: Holder<Biome>): List<BerryBlock> {
            return naturalBerries.filter { berryBlock ->
                val berry = berryBlock.berry()
                berry?.spawnConditions?.any { it.canSpawn(berry, key) } ?: false
            }
        }
    }

    private val naturalBerries = CobblemonBlocks.berries().values.filter {
        (it.berry()?.spawnConditions?.size ?: 0) > 0
    }

    private val validBerryCache: LoadingCache<Holder<Biome>, List<BerryBlock>> = CacheBuilder.newBuilder()
        .maximumSize(4)
        .build(CACHE_LOADER)


    fun getBerriesForBiome(biome: Holder<Biome>): List<BerryBlock> {
        return validBerryCache.get(biome)
    }

    fun getNaturallyGeneratingBerries(): List<BerryBlock> {
        return naturalBerries
    }

}
