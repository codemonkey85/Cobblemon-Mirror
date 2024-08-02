/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.tags

import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey

/**
 * A collection of the Cobblemon [TagKey]s related to the [Registry.BIOME_KEY].
 *
 * @author Veraxiel, Licious
 * @since July 8th, 2022
 */
object CobblemonBiomeTags {

    @JvmField
    val IS_AUTUMN = create("has_season/autumn")
    @JvmField
    val IS_SPRING = create("has_season/spring")
    @JvmField
    val IS_SUMMER = create("has_season/summer")
    @JvmField
    val IS_TEMPERATE = create("is_temperate")
    @JvmField
    val IS_WINTER = create("has_season/winter")

    // Has Feature tags
    @JvmField
    val HAS_APRICORNS_DENSE = create("has_feature/apricorns_dense")
    @JvmField
    val HAS_APRICORNS_NORMAL = create("has_feature/apricorns_normal")
    @JvmField
    val HAS_APRICORNS_SPARSE = create("has_feature/apricorns_sparse")
    @JvmField
    val HAS_REVIVAL_HERBS = create("has_feature/revival_herbs")

    // Has Ore tags
    @JvmField
    val HAS_DAWN_STONE_ORE = create("has_ore/ore_dawn_stone_normal")
    @JvmField
    val HAS_DAWN_STONE_ORE_RARE = create("has_ore/ore_dawn_stone_rare")
    @JvmField
    val HAS_DUSK_STONE_ORE = create("has_ore/ore_dusk_stone_normal")
    @JvmField
    val HAS_DUSK_STONE_ORE_RARE = create("has_ore/ore_dusk_stone_rare")
    @JvmField
    val HAS_FIRE_STONE_ORE = create("has_ore/ore_fire_stone_normal")
    @JvmField
    val HAS_FIRE_STONE_ORE_RARE = create("has_ore/ore_fire_stone_rare")
    @JvmField
    val HAS_FIRE_STONE_ORE_NETHER = create("has_ore/ore_fire_stone_nether")
    @JvmField
    val HAS_ICE_STONE_ORE = create("has_ore/ore_ice_stone_normal")
    @JvmField
    val HAS_ICE_STONE_ORE_RARE = create("has_ore/ore_ice_stone_rare")
    @JvmField
    val HAS_LEAF_STONE_ORE = create("has_ore/ore_leaf_stone_normal")
    @JvmField
    val HAS_LEAF_STONE_ORE_RARE = create("has_ore/ore_leaf_stone_rare")
    @JvmField
    val HAS_MOON_STONE_ORE = create("has_ore/ore_moon_stone_normal")
    @JvmField
    val HAS_MOON_STONE_ORE_RARE = create("has_ore/ore_moon_stone_rare")
    @JvmField
    val HAS_MOON_STONE_ORE_DRIPSTONE = create("has_ore/ore_moon_stone_dripstone")
    @JvmField
    val HAS_SHINY_STONE_ORE = create("has_ore/ore_shiny_stone_normal")
    @JvmField
    val HAS_SHINY_STONE_ORE_RARE = create("has_ore/ore_shiny_stone_rare")
    @JvmField
    val HAS_SUN_STONE_ORE = create("has_ore/ore_sun_stone_normal")
    @JvmField
    val HAS_SUN_STONE_ORE_RARE = create("has_ore/ore_sun_stone_rare")
    @JvmField
    val HAS_THUNDER_STONE_ORE = create("has_ore/ore_thunder_stone_normal")
    @JvmField
    val HAS_THUNDER_STONE_ORE_RARE = create("has_ore/ore_thunder_stone_rare")
    @JvmField
    val HAS_WATER_STONE_ORE = create("has_ore/ore_water_stone_normal")
    @JvmField
    val HAS_WATER_STONE_ORE_RARE = create("has_ore/ore_water_stone_rare")

    private fun create(path: String) = TagKey.create(Registries.BIOME, cobblemonResource(path))
}