/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.influence

import com.cobblemon.mod.common.api.spawning.BestSpawner
import com.cobblemon.mod.common.api.spawning.SpawnBucket
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import com.cobblemon.mod.common.api.spawning.context.calculators.SpawningContextCalculator
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity

/**
 * An influence over spawning that can affect various parts of the spawning process. These can be attached to
 * various areas of the [BestSpawner] for either momentary or extended effects.
 *
 * @author Hiroku
 * @since January 31st, 2022
 */
interface SpawningInfluence {
    /** Whether this influence has passed and should be removed. */
    fun isExpired(): Boolean = false
    /** Returns true if the given spawn detail is able to spawn under this influence. */
    fun affectSpawnable(detail: SpawnDetail, ctx: SpawningContext): Boolean = true
    /** Returns the effective weight of spawning under this influence. This is after typical weight multipliers. */
    fun affectWeight(detail: SpawnDetail, ctx: SpawningContext, weight: Float): Float = weight
    /** Affects the spawn action prior to it generating the entity. */
    fun affectAction(action: SpawnAction<*>) {}
    /** Applies some influence over the entity that's been spawned. */
    fun affectSpawn(entity: Entity) {}
    /** Applies some influence over the weight of spawn buckets. */
    fun affectBucketWeight(bucket: SpawnBucket, weight: Float): Float = weight
    /** Filters positions that could be usable for a spawning context calculator */
    fun isAllowedPosition(world: ServerLevel, pos: BlockPos, contextCalculator: SpawningContextCalculator<*, *>): Boolean = true
}