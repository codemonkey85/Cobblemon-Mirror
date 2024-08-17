/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.condition

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.spawning.context.SeafloorSpawningContext
import com.cobblemon.mod.common.util.Merger
import net.minecraft.world.level.block.Block

/**
 * Base type for a spawning condition that applies to some kind of [SeafloorSpawningContext]. This
 * can be extended for subclasses of [SeafloorSpawningContext].
 *
 * Borrowed the Grounded context code since it was unintentionally spawning Pokémon on the sea floor
 * already. Seems to be working fine /shrug
 *
 * @author FrankTheFarmer
 * @since May 22nd, 2024
 */
abstract class SeafloorTypeSpawningCondition<T : SeafloorSpawningContext> : AreaTypeSpawningCondition<T>() {
    var neededBaseBlocks: MutableList<RegistryLikeCondition<Block>>? = null

    override fun fits(ctx: T): Boolean {
        return if (!super.fits(ctx)) {
            false
        } else if (minHeight != null && ctx.height < minHeight!!) {
            return false
        } else if (maxHeight != null && ctx.height > maxHeight!!) {
            return false
        } else !(neededBaseBlocks != null && neededBaseBlocks!!.none { it.fits(ctx.baseBlock.block, ctx.blockRegistry) })
    }

    override fun copyFrom(other: SpawningCondition<*>, merger: Merger) {
        super.copyFrom(other, merger)
        if (other is SeafloorTypeSpawningCondition) {
            neededBaseBlocks = merger.merge(neededBaseBlocks, other.neededBaseBlocks)?.toMutableList()
        }
    }

    override fun isValid(): Boolean {
        val containsNullValues = neededBaseBlocks != null && neededBaseBlocks!!.any {it == null}
        return super.isValid() && !containsNullValues
    }
}

/**
 * A spawning condition for a [SeafloorSpawningContext].
 *
 * @author FrankTheFarmer
 * @since May 22nd, 2024
 */
open class SeafloorSpawningCondition : SeafloorTypeSpawningCondition<SeafloorSpawningContext>() {
    override fun contextClass() = SeafloorSpawningContext::class.java
    companion object {
        const val NAME = "seafloor"
    }
}