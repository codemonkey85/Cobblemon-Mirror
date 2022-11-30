/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.world.block

import com.cobblemon.mod.common.api.berry.Berry
import com.cobblemon.mod.common.tags.CobblemonBlockTags
import com.cobblemon.mod.common.world.block.entity.BerryBlockEntity
import net.minecraft.block.*
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.IntProperty
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldView

class BerryBlock(val berry: Berry, settings: Settings) : BlockWithEntity(settings), Fertilizable {

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = BerryBlockEntity(pos, state)

    override fun isFertilizable(world: BlockView, pos: BlockPos, state: BlockState, isClient: Boolean) = true

    override fun canGrow(world: World, random: Random, pos: BlockPos, state: BlockState) = state.get(AGE) < MAX_AGE

    override fun hasRandomTicks(state: BlockState) = state.get(AGE) < MAX_AGE

    override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        // Cocoa block uses a 5 here might as well stay consistent
        if (world.random.nextInt(5) == 0) {
            val currentAge = state.get(AGE)
            if (currentAge < MAX_AGE) {
                world.setBlockState(pos, state.with(AGE, currentAge + 1), 2)
            }
        }
    }

    override fun grow(world: ServerWorld, random: Random, pos: BlockPos, state: BlockState) {
        world.setBlockState(pos, state.with(AGE, state.get(AGE) + 1), 2)
    }

    @Deprecated("Deprecated in Java")
    override fun canPlaceAt(state: BlockState, world: WorldView, pos: BlockPos): Boolean {
        return world.getBlockState(pos.down()).isIn(CobblemonBlockTags.BERRY_SOIL)
    }

    @Deprecated("Deprecated in Java")
    override fun getStateForNeighborUpdate(state: BlockState, direction: Direction, neighborState: BlockState, world: WorldAccess, pos: BlockPos, neighborPos: BlockPos): BlockState {
        return if (state.canPlaceAt(world, pos)) super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos) else Blocks.AIR.defaultState
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(AGE)
    }

    companion object {

        private const val MAX_AGE = 5
        val AGE = IntProperty.of("age", 0, MAX_AGE)

    }
}