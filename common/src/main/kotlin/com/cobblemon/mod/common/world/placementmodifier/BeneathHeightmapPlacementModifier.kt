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
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.placement.PlacementContext
import net.minecraft.world.level.levelgen.placement.PlacementModifier
import java.util.*
import java.util.stream.Stream

/**
 * A placement modifier that returns a stream of positions underneath the given heightmap. It will start at some
 * offset and reach a specified number of blocks beneath.
 *
 * @author Hiroku
 * @since June 4th, 2023
 */
class BeneathHeightmapPlacementModifier(val heightmap: Heightmap.Types, val offset: Int, val reach: Int?) : PlacementModifier() {
    companion object {
        val MODIFIER_CODEC: MapCodec<BeneathHeightmapPlacementModifier> = RecordCodecBuilder.mapCodec { instance ->
            instance
                .group(
                    Heightmap.Types.CODEC.fieldOf("heightmap").forGetter { it.heightmap },
                    PrimitiveCodec.INT.fieldOf("offset").forGetter { it.offset },
                    PrimitiveCodec.INT.optionalFieldOf("reach").forGetter { Optional.ofNullable(it.reach) }
                )
                .apply(instance) { heightmap, offset, reach ->
                    BeneathHeightmapPlacementModifier(
                        heightmap,
                        offset,
                        reach.orElse(null)
                    )
                }
        }
    }

    override fun type() = CobblemonPlacementModifierTypes.BENEATH_HEIGHTMAP

    override fun getPositions(context: PlacementContext, random: RandomSource, pos: BlockPos): Stream<BlockPos> {
        var z: Int
        val x = pos.x
        val topY = context.getHeight(heightmap, x, pos.z.also { z = it }) + offset
        val positions = mutableListOf<BlockPos>()

        var y = topY
        while (y > context.minBuildHeight && (reach == null || topY - y < reach)) {
            positions.add(BlockPos(x, y, z))
            y--
        }

        return positions.stream()
    }
}