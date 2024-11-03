/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.world.feature

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.tags.CobblemonBiomeTags
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.BiomeTags
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.placement.PlacedFeature

object CobblemonPlacedFeatures {

    // TODO we don't need a placed feature for every colour, clean all this crap in the JSONs (ask Hiro)
    val BLACK_APRICORN_TREE_PLACED_FEATURE = of("black_apricorn_tree")
    val BLUE_APRICORN_TREE_PLACED_FEATURE = of("blue_apricorn_tree")
    val GREEN_APRICORN_TREE_PLACED_FEATURE = of("green_apricorn_tree")
    val PINK_APRICORN_TREE_PLACED_FEATURE = of("pink_apricorn_tree")
    val RED_APRICORN_TREE_PLACED_FEATURE = of("red_apricorn_tree")
    val WHITE_APRICORN_TREE_PLACED_FEATURE = of("white_apricorn_tree")
    val YELLOW_APRICORN_TREE_PLACED_FEATURE = of("yellow_apricorn_tree")


    val APRICORN_TREES = of("apricorn_trees")
    val SACCHARINE_TREE = of("saccharine_tree")

    val MINTS = of("mints")

    val MEDICINAL_LEEK = of("medicinal_leek")
    val BIG_ROOT = of("big_root")

    val REVIVAL_HERB = of("revival_herb")

    val BERRY_GROVE = of("berry_groves")

    fun register() {
        // We don't need to pass in any tags, the feature implementation handles it, while not a perfect system it works
        Cobblemon.implementation.addFeatureToWorldGen(APRICORN_TREES, GenerationStep.Decoration.VEGETAL_DECORATION, null)
        Cobblemon.implementation.addFeatureToWorldGen(SACCHARINE_TREE, GenerationStep.Decoration.VEGETAL_DECORATION, null)
        Cobblemon.implementation.addFeatureToWorldGen(MINTS, GenerationStep.Decoration.VEGETAL_DECORATION, null)
        Cobblemon.implementation.addFeatureToWorldGen(MEDICINAL_LEEK, GenerationStep.Decoration.VEGETAL_DECORATION, null)
        Cobblemon.implementation.addFeatureToWorldGen(BIG_ROOT, GenerationStep.Decoration.VEGETAL_DECORATION, BiomeTags.IS_OVERWORLD)
        Cobblemon.implementation.addFeatureToWorldGen(REVIVAL_HERB, GenerationStep.Decoration.VEGETAL_DECORATION, CobblemonBiomeTags.HAS_REVIVAL_HERBS)
        Cobblemon.implementation.addFeatureToWorldGen(BERRY_GROVE, GenerationStep.Decoration.VEGETAL_DECORATION, BiomeTags.IS_OVERWORLD)
    }

    private fun of(id: String): ResourceKey<PlacedFeature> = ResourceKey.create(Registries.PLACED_FEATURE, cobblemonResource(id))
}
