/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.world.placementmodifier

import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.serialization.MapCodec
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.levelgen.placement.PlacementModifier
import net.minecraft.world.level.levelgen.placement.PlacementModifierType

object CobblemonPlacementModifierTypes {
    @JvmField
    val BENEATH_HEIGHTMAP = register("beneath_heightmap", BeneathHeightmapPlacementModifier.MODIFIER_CODEC)
    @JvmField
    val LOCATE_PREDICATE = register("locate_predicate", LocatePredicatePlacementModifier.MODIFIER_CODEC)
    @JvmField
    val CONDITIONAL_COUNT = register("conditional_count", ConditionalCountPlacementModifier.MODIFIER_CODEC)
    @JvmField
    val CONDITIONAL_RARITY_FILTER = register("conditional_rarity_filter", ConditionalRarityFilterPlacementModifier.MODIFIER_CODEC)

    fun <T : PlacementModifier> register(id: String, codec: MapCodec<T>): PlacementModifierType<T> {
        return Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, cobblemonResource(id), PlacementModifierType { codec })
    }

    fun touch() = Unit
}
