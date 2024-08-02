/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.condition

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.spawning.context.FishingSpawningContext
import com.cobblemon.mod.common.util.itemRegistry
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block

/**
 * A spawning condition that applies to [FishingSpawningContext]s.
 *
 * @author Hiroku
 * @since February 3rd, 2024
 */
class FishingSpawningCondition: SpawningCondition<FishingSpawningContext>() {
    override fun contextClass() = FishingSpawningContext::class.java

    var rod: RegistryLikeCondition<Item>? = null
    var neededNearbyBlocks: MutableList<RegistryLikeCondition<Block>>? = null

    override fun fits(ctx: FishingSpawningContext): Boolean {
        if (!super.fits(ctx)) {
            return false
        } else if (rod != null && !rod!!.fits(ctx.rodItem ?: return false, ctx.world.itemRegistry)) {
            return false
        } else if (neededNearbyBlocks != null && neededNearbyBlocks!!.none { cond -> ctx.nearbyBlockTypes.any { cond.fits(it, ctx.blockRegistry) } }) {
            return false
        } else {
            return true
        }
    }

    companion object {
        const val NAME = "fishing"
    }
}