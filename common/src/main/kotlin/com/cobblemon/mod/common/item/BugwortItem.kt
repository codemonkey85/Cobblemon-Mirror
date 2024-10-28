/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.block.BugwortBlock
import com.cobblemon.mod.common.block.SaccharineSaplingBlock
import net.minecraft.world.item.ItemNameBlockItem
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.block.state.BlockState

class BugwortItem(block: BugwortBlock) : ItemNameBlockItem(block, Properties()) {

    override fun getPlacementState(context: BlockPlaceContext): BlockState? {

        return super.getPlacementState(context)
    }

}