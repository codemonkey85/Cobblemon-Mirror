/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.berry

import com.cobblemon.mod.common.api.berry.Berry
import com.cobblemon.mod.common.api.berry.GrowthFactor
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

/**
 * The event fired when [Berry.calculateYield] is invoked.
 *
 * @property world The [World] the berry tree is in.
 * @property state The [BlockState] of the berry tree.
 * @property pos The [BlockPos] of the berry tree.
 * @property placer The [LivingEntity] triggering the calculation.
 * @property yield The current yield of berries.
 * @property passedGrowthFactors The [Berry.growthFactors] where [GrowthFactor.isValid] was true.
 *
 * @author Licious
 * @since November 28th, 2022
 */
class BerryYieldCalculationEvent(
    override val berry: Berry,
    val world: Level,
    val state: BlockState,
    val pos: BlockPos,
    val placer: LivingEntity?,
    yield: Int,
    val passedGrowthFactors: Collection<GrowthFactor>
) : BerryEvent {

    /**
     * The amount of berries this tree will yield.
     *
     * @throws IllegalArgumentException If a new value exceeds [Berry.maxYield] or be lesser than 0.
     */
    var yield: Int = yield
        set(value) {
            val max = this.berry.maxYield()
            if (value > max) {
                throw IllegalArgumentException("Cannot set the berry yield for ${this.berry.identifier} above $max")
            }
            if (value < 0) {
                throw IllegalArgumentException("A berry tree cannot yield a negative amount of berries")
            }
            field = value
        }

}