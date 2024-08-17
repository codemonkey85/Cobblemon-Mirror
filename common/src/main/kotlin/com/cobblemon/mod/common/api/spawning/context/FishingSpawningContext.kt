/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.context

import com.cobblemon.mod.common.api.spawning.fishing.FishingSpawnCause
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import com.cobblemon.mod.common.util.toVec3d
import net.minecraft.world.level.block.Block
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB

/**
 * A spawning context that was generated for a fishing action.
 *
 * @author Hiroku
 * @since February 3rd, 2024
 */
class FishingSpawningContext(
    cause: FishingSpawnCause,
    world: ServerLevel,
    pos: BlockPos,
    influences: MutableList<SpawningInfluence>
) : TriggerSpawningContext(
    cause = cause,
    world = world,
    position = pos,
    light = world.getMaxLocalRawBrightness(pos),
    skyLight = world.getMaxLocalRawBrightness(pos.above()),
    canSeeSky = world.canSeeSkyFromBelowWater(pos),
    influences = influences
) {
    val nearbyBlocks = world.getBlockStates(AABB.ofSize(pos.toVec3d(), 10.0, 10.0, 10.0))
    val nearbyBlockTypes: List<Block> by lazy { nearbyBlocks.map { it.block }.distinct().toList() }
    val rodStack = cause.rodStack
    val rodItem = cause.rodItem
    val rodBait = cause.bait
}