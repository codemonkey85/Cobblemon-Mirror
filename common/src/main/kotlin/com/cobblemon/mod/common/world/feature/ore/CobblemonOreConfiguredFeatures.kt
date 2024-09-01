/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.world.feature.ore

import com.cobblemon.mod.common.Cobblemon
import net.minecraft.data.worldgen.features.FeatureUtils
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature

object CobblemonOreConfiguredFeatures {

    val ORE_DAWN_STONE = of("dawn_stone")
    val ORE_DUSK_STONE = of("dusk_stone")
    val ORE_FIRE_STONE = of("fire_stone")
    val ORE_FIRE_STONE_NETHER = of("nether_fire_stone")
    val ORE_ICE_STONE = of("ice_stone")
    val ORE_LEAF_STONE = of("leaf_stone")
    val ORE_MOON_STONE = of("moon_stone")
    // We want this to be a separate configured feature to avoid generating too much moon stone
    val ORE_MOON_STONE_DRIPSTONE = of("dripstone_moon_stone")
    val ORE_SHINY_STONE = of("shiny_stone")
    val ORE_SUN_STONE = of("sun_stone")
    val ORE_THUNDER_STONE = of("thunder_stone")
    val ORE_WATER_STONE = of("water_stone")

    private fun of(id: String): ResourceKey<ConfiguredFeature<*, *>> = FeatureUtils.createKey("${Cobblemon.MODID}:ore/$id")

}