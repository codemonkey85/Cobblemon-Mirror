/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.condition

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.spawning.context.GroundedSpawningContext
import com.cobblemon.mod.common.util.Merger
import net.minecraft.world.level.block.Block

/**
 * Base type for a spawning condition that applies to some kind of [GroundedSpawningContext]. This
 * can be extended for subclasses of [GroundedSpawningContext].
 *
 * @author Hiroku
 * @since February 7th, 2022
 */
abstract class GroundedTypeSpawningCondition<T : GroundedSpawningContext> : AreaTypeSpawningCondition<T>() {
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
        if (other is GroundedTypeSpawningCondition) {
            neededBaseBlocks = merger.merge(neededBaseBlocks, other.neededBaseBlocks)?.toMutableList()
        }
    }

    override fun isValid(): Boolean {
        val containsNullValues = neededBaseBlocks != null && neededBaseBlocks!!.any {it == null}
        return super.isValid() && !containsNullValues
    }
}

/**
 * A spawning condition for a [GroundedSpawningContext].
 *
 * @author Hiroku
 * @since February 7th, 2022
 */
open class GroundedSpawningCondition : GroundedTypeSpawningCondition<GroundedSpawningContext>() {
    override fun contextClass() = GroundedSpawningContext::class.java
    companion object {
        const val NAME = "grounded"
    }
}