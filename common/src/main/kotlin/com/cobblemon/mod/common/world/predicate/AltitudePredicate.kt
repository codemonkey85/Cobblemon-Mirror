/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.world.predicate

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate
import java.util.*

class AltitudePredicate(val min: Optional<Int>, val max: Optional<Int>) : BlockPredicate {
    val range = min.orElse(Int.MIN_VALUE)..max.orElse(Int.MAX_VALUE)

    override fun test(world: WorldGenLevel, block: BlockPos) = block.y in range

    override fun type() = CobblemonBlockPredicates.ALTITUDE

    companion object {
        val CODEC : MapCodec<AltitudePredicate> = RecordCodecBuilder.mapCodec {
            it.group(
                PrimitiveCodec.INT.optionalFieldOf("min").forGetter(AltitudePredicate::min),
                PrimitiveCodec.INT.optionalFieldOf("max").forGetter(AltitudePredicate::max)
            ).apply(it, ::AltitudePredicate)
        }
    }
}