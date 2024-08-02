/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.context.calculators

import com.cobblemon.mod.common.Cobblemon.config
import com.cobblemon.mod.common.api.spawning.WorldSlice
import com.cobblemon.mod.common.api.spawning.context.AreaContextResolver
import com.cobblemon.mod.common.api.spawning.context.AreaSpawningContext
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import com.cobblemon.mod.common.api.spawning.spawner.Spawner
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState

/**
 * A spawning context calculator that deals with [AreaSpawningContext]s. These work off
 * [AreaSpawningInput] instances, and must output some kind of [AreaSpawningContext].
 *
 * This is the interface to use for all of the contexts that will get used by world spawning
 * as that is the primary case that area spawning is done. These get registered when
 * creating the context type, in [SpawningContext.register]
 *
 * @author Hiroku
 * @since January 31st, 2022
 */
interface AreaSpawningContextCalculator<O : AreaSpawningContext> : SpawningContextCalculator<AreaSpawningInput, O> {
    /**
     * Whether or not this context calculator is likely to provide a value for this location.
     *
     * This should be relatively final. See how the [AreaContextResolver] works to understand why
     * you should be pretty sure before returning true to this function.
     */
    fun fits(input: AreaSpawningInput): Boolean

    fun getDepth(input: AreaSpawningInput, condition: (BlockState) -> Boolean, maximum: Int): Int
        = input.slice.depthSpace(input.position.x, input.position.y, input.position.z, condition, maximum)
    fun getHeight(input: AreaSpawningInput, condition: (BlockState) -> Boolean, maximum: Int, offsetX: Int = 0, offsetY: Int = 0, offsetZ: Int = 0): Int
        = input.slice.heightSpace(input.position.x + offsetX, input.position.y + offsetY, input.position.z + offsetZ, condition, maximum)
    fun getHorizontalSpace(input: AreaSpawningInput, condition: (BlockState) -> Boolean, maximum: Int, offsetX: Int = 0, offsetY: Int = 0, offsetZ: Int = 0): Int
        = input.slice.horizontalSpace(input.position.x + offsetX, input.position.y + offsetY, input.position.z + offsetZ, condition, maximum)
    fun getLight(input: AreaSpawningInput, elseLight: Int = 0): Int
        = input.slice.getLight(input.position.x, input.position.y + 1, input.position.z, elseLight)
    fun getSkyLight(input: AreaSpawningInput, elseLight: Int = 0): Int
        = input.slice.getSkyLight(input.position.x, input.position.y + 1, input.position.z, elseLight)
    fun getCanSeeSky(input: AreaSpawningInput): Boolean = input.slice.canSeeSky(input.position.x, input.position.y + 1, input.position.z)
    fun getSkySpaceAbove(input: AreaSpawningInput): Int = input.slice.skySpaceAbove(input.position.x, input.position.y, input.position.z)
    fun getNearbyBlocks(
        input: AreaSpawningInput,
        horizontalRadius: Int = config.maxNearbyBlocksHorizontalRange,
        verticalRadius: Int = config.maxNearbyBlocksVerticalRange
    ) = input.slice.nearbyBlocks(input.position, horizontalRadius, verticalRadius)

}

open class AreaSpawningInput(val spawner: Spawner, var position: BlockPos, val slice: WorldSlice) : SpawningContextInput(slice.cause, slice.world)