/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.world.structureprocessors

import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.serialization.MapCodec
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType

object CobblemonProcessorTypes {
    val registry = BuiltInRegistries.STRUCTURE_PROCESSOR
    val lists = CobblemonStructureProcessorLists

    @JvmField
    val RANDOM_POOLED_STATES = register("random_pooled_states", RandomizedStructureMappedBlockStatePairProcessor.CODEC)

    fun <T : StructureProcessor> register(id: String, codec: MapCodec<T>): StructureProcessorType<T> {
        return Registry.register(registry, cobblemonResource(id), StructureProcessorType { codec })
    }

    fun touch() = Unit
}
