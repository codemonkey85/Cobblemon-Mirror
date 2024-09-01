/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.fishing

import com.cobblemon.mod.common.api.spawning.CobblemonSpawnPools
import com.cobblemon.mod.common.api.spawning.context.FishingSpawningContext
import com.cobblemon.mod.common.api.spawning.detail.SpawnPool
import com.cobblemon.mod.common.api.spawning.spawner.TriggerSpawner
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos

/**
 * A spawner that takes in a [FishingSpawnCause] and spawns things.
 *
 * @author Hiroku
 * @since February 3rd, 2024
 */
open class FishingSpawner(
    override val name: String = "fishing",
    pool: SpawnPool = CobblemonSpawnPools.WORLD_SPAWN_POOL
) : TriggerSpawner<FishingSpawnCause>(name, pool) {
    override fun parseContext(cause: FishingSpawnCause, world: ServerLevel, pos: BlockPos): FishingSpawningContext? {
        // Maybe confirm that it's water we're fishing in?
        val ctx = FishingSpawningContext(
            cause = cause,
            world = world,
            pos = pos,
            influences = influences
        )
        return ctx
    }
}