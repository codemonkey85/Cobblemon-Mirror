/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.multiblock.builder

import com.cobblemon.mod.common.api.multiblock.condition.MultiblockCondition
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.shapes.VoxelShape

/**
 * Represents an area that has a potential to form a MultiBlockStructure
 * @property boundingBox The box that each condition checks in
 * @property conditions The [MultiblockCondition]s that must be met for the multiblock to form. All must be true.
 *
 * @author Apion
 * @since August 24, 2023
 */
interface MultiblockStructureBuilder {
    val boundingBox: VoxelShape
    val conditions: List<MultiblockCondition>

    fun validate(world: ServerLevel): Boolean {
        conditions.forEach {
            if (!it.test(world, boundingBox)) {
                return false
            }
        }
        form(world)
        return true
    }

    fun form(world: ServerLevel)
}
