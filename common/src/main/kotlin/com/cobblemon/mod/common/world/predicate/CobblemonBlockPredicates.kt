/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.world.predicate

import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.serialization.MapCodec
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType

object CobblemonBlockPredicates {
    @JvmField
    val ALTITUDE = register("altitude", AltitudePredicate.CODEC)
    val BIOME = register("biome", BiomePredicate.CODEC)

    fun <P : BlockPredicate?> register(id: String, codec: MapCodec<P>): BlockPredicateType<P> {
        return Registry.register(BuiltInRegistries.BLOCK_PREDICATE_TYPE, cobblemonResource(id), BlockPredicateType { codec })
    }

    fun touch() = Unit
}