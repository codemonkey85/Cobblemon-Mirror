/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.context.calculators

import com.cobblemon.mod.common.Cobblemon.config
import com.cobblemon.mod.common.api.spawning.context.SubmergedSpawningContext
import com.cobblemon.mod.common.api.spawning.context.calculators.SpawningContextCalculator.Companion.isLavaCondition
import com.cobblemon.mod.common.api.spawning.context.calculators.SpawningContextCalculator.Companion.isWaterCondition
import net.minecraft.util.Mth.ceil
import net.minecraft.world.level.block.state.BlockState

/**
 * The context calculator used for [SubmergedSpawningContext]s. Requires a fluid block as a base and the same fluid
 * block in surrounding space.
 *
 * @author Hiroku
 * @since February 7th, 2022
 */
object SubmergedSpawningContextCalculator : AreaSpawningContextCalculator<SubmergedSpawningContext> {
    override val name = "submerged"
    val fluidConditions = mutableListOf(
        isWaterCondition,
        isLavaCondition
    )

    override fun fits(input: AreaSpawningInput): Boolean {
        val condition = getFluidCondition(input)
        // For it to fit, there must be a known fluid above and below the base block. That's what defines submerged.
        return condition != null && condition(input.slice.getBlockState(input.position.below())) && condition(input.slice.getBlockState(input.position.above()))
    }

    fun getFluidCondition(input: AreaSpawningInput): ((BlockState) -> Boolean)? {
        return fluidConditions.firstOrNull { it(input.slice.getBlockState(input.position)) }
    }

    override fun calculate(input: AreaSpawningInput): SubmergedSpawningContext {
        val fluidCondition = getFluidCondition(input)!!
        return SubmergedSpawningContext(
            cause = input.cause,
            world = input.world,
            position = input.position.immutable(),
            light = getLight(input),
            skyLight = getSkyLight(input),
            canSeeSky = getCanSeeSky(input),
            influences = input.spawner.copyInfluences(),
            height = getHeight(input, fluidCondition, ceil(config.maxVerticalSpace / 2F)),
            depth = getDepth(input, fluidCondition, ceil(config.maxVerticalSpace / 2F)),
            slice = input.slice,
            nearbyBlocks = getNearbyBlocks(input)
        )
    }
}

