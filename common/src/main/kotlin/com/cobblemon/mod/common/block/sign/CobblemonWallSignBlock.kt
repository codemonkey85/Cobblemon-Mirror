/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.sign

import com.cobblemon.mod.common.block.entity.CobblemonSignBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.WallSignBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.WoodType

class CobblemonWallSignBlock(settings: Properties, woodType: WoodType) : WallSignBlock(woodType, settings) {

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = CobblemonSignBlockEntity(pos, state)

}