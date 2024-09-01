/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.berry

import com.cobblemon.mod.common.api.berry.GrowthFactor
import com.cobblemon.mod.common.block.BerryBlock
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.core.BlockPos
import net.minecraft.world.level.LevelReader

class PreferredBiomeGrowthFactor(
    val bonusYield: IntRange
) : GrowthFactor{
    override fun validateArguments() {
        if (this.bonusYield.first < 0 || this.bonusYield.last < 0) {
            throw IllegalArgumentException("$ID bonusYield must be a positive range")
        }
    }

    override fun isValid(world: LevelReader, state: BlockState, pos: BlockPos): Boolean {
        val biome = world.getBiome(pos)
        val block = state.block as BerryBlock
        val biomeTags = block.berry()?.preferredBiomeTags ?: emptyList()

        return biomeTags.any { biome.`is`(it) }
    }

    override fun yield() = this.bonusYield.random()

    override fun minYield() = this.bonusYield.first

    override fun maxYield() = this.bonusYield.last

    companion object {

        val ID = cobblemonResource("preferred_biome")

    }

}
