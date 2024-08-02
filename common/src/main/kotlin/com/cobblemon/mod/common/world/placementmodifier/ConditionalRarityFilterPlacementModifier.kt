/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.world.placementmodifier

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate
import net.minecraft.world.level.levelgen.placement.PlacementContext
import net.minecraft.world.level.levelgen.placement.PlacementFilter

/**
 * It's like the rarity filter placement modifier, but conditional. If condition fails, doesn't alter the stream.
 *
 * @author Hiroku
 * @since September 23rd, 2023
 */
class ConditionalRarityFilterPlacementModifier(
    val predicate: BlockPredicate,
    val chance: Int
) : PlacementFilter() {
    companion object {
        val MODIFIER_CODEC: MapCodec<ConditionalRarityFilterPlacementModifier> = RecordCodecBuilder.mapCodec { instance ->
            instance
                .group(
                    BlockPredicate.CODEC.fieldOf("predicate").forGetter { it.predicate },
                    PrimitiveCodec.INT.fieldOf("chance").forGetter { it.chance }
                )
                .apply(instance, ::ConditionalRarityFilterPlacementModifier)
        }
    }

    override fun type() = CobblemonPlacementModifierTypes.CONDITIONAL_RARITY_FILTER

    override fun shouldPlace(context: PlacementContext, random: RandomSource, pos: BlockPos): Boolean {
        return if (predicate.test(context.level, pos)) random.nextFloat() < 1.0f / this.chance.toFloat() else true
    }
}