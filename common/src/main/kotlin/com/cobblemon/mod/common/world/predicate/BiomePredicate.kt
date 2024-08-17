/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.world.predicate

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate
import java.util.*
import kotlin.jvm.optionals.getOrNull

/**
 * A [BlockPredicate] that is satisfied if the [Biome] of the [BlockPos] is listed in [includedBiomes] and/or not in [excludedBiomes].
 *
 * @author miasmus
 * @since September 12th, 2023
 */
class BiomePredicate(
    val includedBiomes: Optional<MutableList<TagKey<Biome>>>,
    val excludedBiomes: Optional<MutableList<TagKey<Biome>>>
) : BlockPredicate {

    override fun test(world: WorldGenLevel, block: BlockPos): Boolean {
        val biome = world.getBiome(block)
        // If biomes are not specified, default to true -- exclusions override inclusions
        return (
                (includedBiomes.getOrNull()?.any { biome.`is`(it) } ?: true) &&
                !(excludedBiomes.getOrNull()?.any { biome.`is`(it) } ?: false))
    }

    override fun type() = CobblemonBlockPredicates.BIOME

    companion object {
        val CODEC : MapCodec<BiomePredicate> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                TagKey.hashedCodec(Registries.BIOME).listOf().optionalFieldOf("includedBiomes").forGetter { it.includedBiomes },
                TagKey.hashedCodec(Registries.BIOME).listOf().optionalFieldOf("excludedBiomes").forGetter { it.excludedBiomes }
            ).apply(instance, ::BiomePredicate)
        }
    }
}