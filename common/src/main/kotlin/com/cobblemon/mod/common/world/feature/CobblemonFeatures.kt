/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.world.feature

import com.cobblemon.mod.common.platform.PlatformRegistry
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.levelgen.feature.Feature

object CobblemonFeatures : PlatformRegistry<Registry<Feature<*>>, ResourceKey<Registry<Feature<*>>>, Feature<*>>() {

    override val registry: Registry<Feature<*>> = BuiltInRegistries.FEATURE
    override val resourceKey: ResourceKey<Registry<Feature<*>>> = Registries.FEATURE

    @JvmField
    val APRICORN_TREE_FEATURE = create("apricorn_tree", ApricornTreeFeature())
    @JvmField
    val SACCHARINE_TREE_FEATURE = create("saccharine_tree", SaccharineTreeFeature())
    @JvmField
    val MINT_FEATURE = create("mint", MintBlockFeature())
    @JvmField
    val BERRY_GROVE_FEATURE = CobblemonFeatures.create("berry_grove", BerryGroveFeature())
}
