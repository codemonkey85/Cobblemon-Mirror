/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.multiblock

import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.player.Player
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

interface MultiblockStructure {
    val controllerBlockPos: BlockPos

    fun useWithoutItem(
        blockState: BlockState,
        world: Level,
        blockPos: BlockPos,
        player: Player,
        blockHitResult: BlockHitResult
    ): InteractionResult

    fun playerWillDestroy(world: Level, pos: BlockPos, state: BlockState, player: Player?)

    fun tick(world: Level)

    fun syncToClient(world: Level)

    fun markDirty(world: Level)
    fun writeToNbt(registryLookup: HolderLookup.Provider): CompoundTag
    fun getAnalogOutputSignal(state: BlockState, world: Level?, pos: BlockPos?): Int {
        return 0
    }

    fun setRemoved(world: Level)
    fun onTriggerEvent(state: BlockState?, world: ServerLevel?, pos: BlockPos?, random: RandomSource?)
}