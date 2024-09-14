/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.platform.PlatformRegistry
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.common.collect.ImmutableSet
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.ai.village.poi.PoiType
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

object CobblemonVillagerPoiTypes: PlatformRegistry<Registry<PoiType>, ResourceKey<Registry<PoiType>>, PoiType>() {
    override val registry: Registry<PoiType> = BuiltInRegistries.POINT_OF_INTEREST_TYPE
    override val resourceKey: ResourceKey<Registry<PoiType>> = Registries.POINT_OF_INTEREST_TYPE

    @JvmField
    val NURSE_KEY: ResourceKey<PoiType> = createKey("nurse")
    @JvmField
    val NURSE = create(NURSE_KEY.location().path, PoiType(getBlockStates(CobblemonBlocks.HEALING_MACHINE), 1, 1))

    private fun createKey(string: String): ResourceKey<PoiType> = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, cobblemonResource(string))

    private fun getBlockStates(block: Block): Set<BlockState> = ImmutableSet.copyOf(block.stateDefinition.possibleStates)
}
