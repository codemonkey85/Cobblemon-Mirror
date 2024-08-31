/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.context

import com.cobblemon.mod.common.api.spawning.SpawnCause
import com.cobblemon.mod.common.api.spawning.WorldSlice
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import kotlin.math.ceil
import kotlin.math.floor
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos

/**
 * A [SpawningContext] that is for a particular area, and therefore has spatial properties.
 *
 * @author Hiroku
 * @since January 31st, 2022
 */
open class AreaSpawningContext(
    override val cause: SpawnCause,
    override val world: ServerLevel,
    override val position: BlockPos,
    override val light: Int,
    override val skyLight: Int,
    override val canSeeSky: Boolean,
    override val influences: MutableList<SpawningInfluence>,
    /** Space above. */
    val height: Int,
    val nearbyBlocks: List<BlockState>,
    val slice: WorldSlice
) : SpawningContext() {
    val nearbyBlockTypes: List<Block> by lazy { nearbyBlocks.mapNotNull { it.block }.distinct() }

    override fun getStructureCache(pos: BlockPos): StructureChunkCache {
        return slice.getStructureCache(pos)
    }

    /**
     * Returns true if the given block can be occupied by a spawn in this context.
     * This is not considering the provided state as what the entity would be on top of,
     * but rather the space its hitbox would fill.
     */
    open fun isSafeSpace(world: ServerLevel, pos: BlockPos, state: BlockState): Boolean = !state.isSolid

    override fun postFilter(detail: SpawnDetail): Boolean {
        if (!super.postFilter(detail)) {
            return false
        }

        if (detail.width > 1 || detail.height > 1) {
            val sizeX = detail.width.takeIf { it > 0 } ?: 1
            val sizeY = detail.height.takeIf { it > 0 } ?: 1

            val minX = floor(position.x + 0.5 - (sizeX - 1) / 2F).toInt() - 1
            val maxX = ceil(position.x + 0.5 + (sizeX + 1) / 2F).toInt() + 1

            val maxY = ceil(position.y + (sizeY + 1) / 2F).toInt() + 1

            val minZ = floor(position.z + 0.5 - (sizeX - 1) / 2F).toInt() - 1
            val maxZ = ceil(position.z + 0.5 + (sizeX + 1) / 2F).toInt() + 1

            val mutable = BlockPos.MutableBlockPos()
            for (x in minX until maxX) {
                for (y in (position.y + 1)..maxY) {
                    for (z in minZ until maxZ) {
                        val state = world.getBlockState(mutable.set(x, y, z))
                        if (!isSafeSpace(world, mutable, state)) {
                            return false
                        }
                    }
                }
            }
        }

        return true
    }
}