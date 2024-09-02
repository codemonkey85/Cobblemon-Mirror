/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.farming

import com.cobblemon.mod.common.api.apricorn.Apricorn
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.state.BlockState

/**
 * Event fired when an Apricorn is harvested.
 */
class ApricornHarvestEvent(
    val player: ServerPlayer,
    val apricorn: Apricorn,
    val world: ServerLevel,
    val pos: BlockPos
) {
    fun getBlock(): BlockState {
        return world.getBlockState(pos)
    }
}