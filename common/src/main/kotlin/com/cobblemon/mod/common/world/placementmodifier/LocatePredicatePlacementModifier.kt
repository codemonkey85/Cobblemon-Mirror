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
import net.minecraft.world.level.levelgen.placement.PlacementModifier
import net.minecraft.world.level.levelgen.placement.PlacementModifierType
import java.util.stream.Stream

class LocatePredicatePlacementModifier(
    val predicate: BlockPredicate,
    val maxTries: Int,
    val xzRange: Int,
    val yRange: Int
) : PlacementModifier() {
    companion object {
        val MODIFIER_CODEC: MapCodec<LocatePredicatePlacementModifier> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                BlockPredicate.CODEC.fieldOf("predicate").forGetter {it.predicate},
                PrimitiveCodec.INT.fieldOf("maxTries").forGetter {it.maxTries},
                PrimitiveCodec.INT.fieldOf("xzRange").forGetter {it.xzRange},
                PrimitiveCodec.INT.fieldOf("yRange").forGetter {it.yRange}
            )
            .apply(instance) {predicate, maxTries, xzRange, yRange ->
                LocatePredicatePlacementModifier(predicate, maxTries, xzRange, yRange)
            }
        }
    }
    override fun getPositions(
        context: PlacementContext,
        random: RandomSource,
        pos: BlockPos
    ): Stream<BlockPos> {
        for (i in 0..maxTries) {
            val newPos = pos.offset(random.nextIntBetweenInclusive(0, xzRange), random.nextIntBetweenInclusive(-yRange, yRange), random.nextIntBetweenInclusive(0, xzRange))
            if (predicate.test(context.level, newPos)) {
                return Stream.of(newPos)
            }
        }
        return Stream.empty()
    }

    override fun type(): PlacementModifierType<*> = CobblemonPlacementModifierTypes.LOCATE_PREDICATE
}
