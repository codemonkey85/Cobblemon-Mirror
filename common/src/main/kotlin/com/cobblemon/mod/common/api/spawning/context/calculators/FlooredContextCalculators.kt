/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.context.calculators

import com.cobblemon.mod.common.Cobblemon.config
import com.cobblemon.mod.common.api.spawning.context.*
import com.cobblemon.mod.common.api.spawning.context.calculators.SpawningContextCalculator.Companion.isAirCondition
import com.cobblemon.mod.common.api.spawning.context.calculators.SpawningContextCalculator.Companion.isLavaCondition
import com.cobblemon.mod.common.api.spawning.context.calculators.SpawningContextCalculator.Companion.isSolidCondition
import com.cobblemon.mod.common.api.spawning.context.calculators.SpawningContextCalculator.Companion.isWaterCondition
import net.minecraft.world.level.block.state.BlockState

/**
 * A spawning context calculator that creates some kind of [FlooredSpawningContext]. The shared
 * idea of these contexts is that there is a base block condition for the floor, and then some
 * other condition for its surroundings.
 *
 * @author Hiroku
 * @since February 7th, 2022
 */
interface FlooredSpawningContextCalculator<T : FlooredSpawningContext> : AreaSpawningContextCalculator<T> {
    /** The condition that must be met by the base block. */
    val baseCondition: (BlockState) -> Boolean
    /** The condition that must be met by the surrounding blocks. */
    val surroundingCondition: (BlockState) -> Boolean

    override fun fits(input: AreaSpawningInput): Boolean {
        val floorState = input.slice.getBlockState(input.position)
        val aboveState = input.slice.getBlockState(input.position.above())
        return baseCondition(floorState) && surroundingCondition(aboveState)
    }
}

/**
 * The context calculator used for [GroundedSpawningContext]s. Requires a solid block below it and
 * air blocks in its surroundings.
 *
 * @author Hiroku
 * @since February 7th, 2022
 */
object GroundedSpawningContextCalculator : FlooredSpawningContextCalculator<GroundedSpawningContext> {
    // TODO expand base condition
    override val name = "grounded"
    override val baseCondition: (BlockState) -> Boolean = isSolidCondition
    override val surroundingCondition: (BlockState) -> Boolean = isAirCondition

    override fun calculate(input: AreaSpawningInput): GroundedSpawningContext {
        return GroundedSpawningContext(
            cause = input.cause,
            world = input.world,
            position = input.position.immutable(),
            light = getLight(input),
            skyLight = getSkyLight(input),
            canSeeSky = getCanSeeSky(input),
            influences = input.spawner.copyInfluences(),
            height = getHeight(input, surroundingCondition, config.maxVerticalSpace, offsetY = 1),
            slice = input.slice,
            nearbyBlocks = getNearbyBlocks(input)
        )
    }
}

/**
 * The context calculator used for [SeafloorSpawningContext]s. Requires a solid block below it and
 * water blocks in its surroundings.
 *
 * @author Hiroku
 * @since February 7th, 2022
 */
object SeafloorSpawningContextCalculator : FlooredSpawningContextCalculator<SeafloorSpawningContext> {
    override val name = "seafloor"
    override val baseCondition: (BlockState) -> Boolean = isSolidCondition
    override val surroundingCondition: (BlockState) -> Boolean = isWaterCondition

    override fun calculate(input: AreaSpawningInput): SeafloorSpawningContext {
        return SeafloorSpawningContext(
            cause = input.cause,
            world = input.world,
            position = input.position.immutable(),
            light = getLight(input),
            skyLight = getSkyLight(input),
            canSeeSky = getCanSeeSky(input),
            influences = input.spawner.copyInfluences(),
            height = getHeight(input, surroundingCondition, config.maxVerticalSpace, offsetY = 1),
            slice = input.slice,
            nearbyBlocks = getNearbyBlocks(input)
        )
    }
}

/**
 * The context calculator used for [LavafloorSpawningContext]s. Requires a solid block below it and
 * lava blocks in its surroundings.
 *
 * @author Hiroku
 * @since February 7th, 2022
 */
object LavafloorSpawningContextCalculator : FlooredSpawningContextCalculator<LavafloorSpawningContext> {
    override val name = "lavafloor"
    override val baseCondition: (BlockState) -> Boolean = isSolidCondition
    override val surroundingCondition: (BlockState) -> Boolean = isLavaCondition

    override fun calculate(input: AreaSpawningInput): LavafloorSpawningContext {
        return LavafloorSpawningContext(
            cause = input.cause,
            world = input.world,
            position = input.position.immutable(),
            light = getLight(input),
            skyLight = getSkyLight(input),
            canSeeSky = getCanSeeSky(input),
            influences = input.spawner.copyInfluences(),
            height = getHeight(input, surroundingCondition, config.maxVerticalSpace, offsetY = 1),
            slice = input.slice,
            nearbyBlocks = getNearbyBlocks(input)
        )
    }
}


/**
 * The context calculator used for [SurfaceSpawningContext]s. Requires a fluid block below it and
 * air blocks in its surroundings.
 *
 * @author Hiroku
 * @since December 15th, 2022
 */
object SurfaceSpawningContextCalculator : FlooredSpawningContextCalculator<SurfaceSpawningContext> {
    override val name = "surface"
    override val baseCondition: (BlockState) -> Boolean = { !it.fluidState.isEmpty }
    override val surroundingCondition: (BlockState) -> Boolean = isAirCondition

    override fun calculate(input: AreaSpawningInput): SurfaceSpawningContext {
        return SurfaceSpawningContext(
            cause = input.cause,
            world = input.world,
            position = input.position.immutable(),
            light = getLight(input),
            skyLight = getSkyLight(input),
            canSeeSky = getCanSeeSky(input),
            influences = input.spawner.copyInfluences(),
            height = getHeight(input, surroundingCondition, config.maxVerticalSpace / 2, offsetY = 1),
            depth = getDepth(input, baseCondition, config.maxVerticalSpace / 2),
            slice = input.slice,
            nearbyBlocks = getNearbyBlocks(input)
        )
    }
}

