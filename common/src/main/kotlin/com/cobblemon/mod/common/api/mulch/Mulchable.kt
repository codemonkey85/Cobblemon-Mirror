/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.mulch

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.state.BlockState

/**
 * Represents something that can have mulch applied to it.
 * Typically, this will be a block implementation.
 */
interface Mulchable {

    /**
     * Checks if mulch can be applied to a specific spot in the world.
     *
     * @param world The affected [ServerLevel].
     * @param pos The [BlockPos] being checked.
     * @param state The [BlockState] of the block at the [pos] in the [world].
     * @param variant The [MulchVariant] being applied.
     * @return If the application is possible.
     */
    fun canHaveMulchApplied(world: ServerLevel, pos: BlockPos, state: BlockState, variant: MulchVariant): Boolean

    /**
     * Applies the mulch to the specified spot.
     * This is expected to be invoked if [canHaveMulchApplied] is true.
     *
     * @param world The affected [ServerLevel].
     * @param random The [Random] instance being used in the interaction.
     * @param pos The [BlockPos] being checked.
     * @param state The [BlockState] of the block at the [pos] in the [world].
     * @param variant The [MulchVariant] being applied.
     */
    fun applyMulch(world: ServerLevel, random: RandomSource, pos: BlockPos, state: BlockState, variant: MulchVariant)

}