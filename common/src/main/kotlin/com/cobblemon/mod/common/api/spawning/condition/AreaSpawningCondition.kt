/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.condition

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.spawning.context.AreaSpawningContext
import com.cobblemon.mod.common.util.Merger
import net.minecraft.world.level.block.Block

/**
 * Base type for a spawning condition that applies to some kind of [AreaSpawningContext]. This
 * can be extended for subclasses of [AreaSpawningContext].
 *
 * @author Hiroku
 * @since February 7th, 2022
 */
abstract class AreaTypeSpawningCondition<T : AreaSpawningContext> : SpawningCondition<T>() {
    var minHeight: Int? = null
    var maxHeight: Int? = null
    var neededNearbyBlocks: MutableList<RegistryLikeCondition<Block>>? = null

    override fun fits(ctx: T): Boolean {
        if (!super.fits(ctx)) {
            return false
        } else if (minHeight != null && ctx.height < minHeight!!) {
            return false
        } else if (maxHeight != null && ctx.height > maxHeight!!) {
            return false
        } else if (neededNearbyBlocks != null && neededNearbyBlocks!!.none { cond -> ctx.nearbyBlockTypes.any { cond.fits(it, ctx.blockRegistry) } }) {
            return false
        } else {
            return true
        }
    }

    override fun copyFrom(other: SpawningCondition<*>, merger: Merger) {
        super.copyFrom(other, merger)
        if (other is AreaTypeSpawningCondition) {
            merger.mergeSingle(minHeight, other.minHeight)
            merger.mergeSingle(maxHeight, other.maxHeight)
            neededNearbyBlocks = merger.merge(neededNearbyBlocks, other.neededNearbyBlocks)?.toMutableList()
        }
    }

    override fun isValid(): Boolean {
        val containsNullValues = neededNearbyBlocks != null && neededNearbyBlocks!!.any {it == null}
        return super.isValid() && !containsNullValues
    }
}

/**
 * A spawning condition for an [AreaSpawningContext].
 *
 * @author Hiroku
 * @since February 7th, 2022
 */
class AreaSpawningCondition : AreaTypeSpawningCondition<AreaSpawningContext>() {
    override fun contextClass(): Class<out AreaSpawningContext> = AreaSpawningContext::class.java
    companion object {
        const val NAME = "area"
    }
}