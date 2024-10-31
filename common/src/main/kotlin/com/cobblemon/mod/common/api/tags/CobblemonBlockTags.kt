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
 * A collection of the Cobblemon [TagKey]s related to the [Registries.BLOCK].
 *
 * @author Licious
 * @since October 29th, 2022
 */
@Suppress("HasPlatformType", "unused")
object CobblemonBlockTags {

    @JvmField val ALL_HANGING_SIGNS = createTag("all_hanging_signs")
    @JvmField val ALL_SIGNS = createTag("all_signs")
    @JvmField val APRICORN_LEAVES = createTag("apricorn_leaves")
    @JvmField val APRICORN_LOGS = createTag("apricorn_logs")
    @JvmField val APRICORN_SAPLINGS = createTag("apricorn_saplings")
    @JvmField val APRICORNS = createTag("apricorns")
    @JvmField val BERRY_WILD_SOIL = createTag("berry_wild_soil")
    @JvmField val BERRY_SOIL = createTag("berry_soil")
    @JvmField val BERRY_REPLACEABLE = createTag("berry_replaceable")
    @JvmField val CEILING_HANGING_SIGNS = createTag("ceiling_hanging_signs")
    @JvmField val CROPS = createTag("crops")
    @JvmField val DRIPSTONE_GROWABLE = createTag("dripstone_growable")
    @JvmField val DRIPSTONE_REPLACEABLES = createTag("dripstone_replaceables")
    @JvmField val FLOWERS = createTag("flowers")
    @JvmField val MEDICINAL_LEEK_PLANTABLE = createTag("medicinal_leek_plantable")
    @JvmField val MINTS = createTag("mints")
    @JvmField val ROOTS_SPREADABLE = createTag("roots_spreadable")
    @JvmField val SIGNS = createTag("signs")
    @JvmField val SMALL_FLOWERS = createTag("small_flowers")
    @JvmField val SEES_SKY = createTag("sees_sky")
    @JvmField val SNOW_BLOCK = createTag("snow_block")
    @JvmField val ROOTS = createTag("roots")
    @JvmField val STANDING_SIGNS = createTag("standing_signs")
    @JvmField val TUMBLESTONE_HEAT_SOURCE = createTag("tumblestone_heat_source")
    @JvmField val WALL_HANGING_SIGNS = createTag("wall_hanging_signs")
    @JvmField val WALL_SIGNS = createTag("wall_signs")

    private fun createTag(name: String) = TagKey.create(Registries.BLOCK, cobblemonResource(name))

}
