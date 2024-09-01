/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.berry

import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.state.BlockState

/**
 * Represents a condition that impacts extra berry yield.
 *
 * @author Licious
 * @since December 2nd, 2022
 */
interface GrowthFactor {

    /**
     * Validates the arguments contained in this factor, this is invoked during loading.
     *
     * @throws IllegalArgumentException If an argument isn't valid.
     */
    fun validateArguments()

    /**
     * Checks if this factor should yield a bonus.
     *
     * @param world The [WorldView] the berry tree is in.
     * @param state The [BlockState] of the berry tree.
     * @param pos The [BlockPos] of the berry tree.
     * @return If the bonus should activate.
     */
    fun isValid(world: LevelReader, state: BlockState, pos: BlockPos): Boolean

    /**
     * Resolves the amount of bonus berries to grow if [isValid] was true.
     *
     * @return The amount of extra berries to grow.
     */
    fun yield(): Int

    /**
     * Returns the minimum possible yield for this factor.
     *
     * @return The minimum possible yield for this factor.
     */
    fun minYield(): Int

    /**
     * Returns the maximum possible yield for this factor.
     *
     * @return The maximum possible yield for this factor.
     */
    fun maxYield(): Int

}