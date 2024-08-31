/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.spawner

import com.cobblemon.mod.common.api.spawning.SpawnCause
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import com.cobblemon.mod.common.api.spawning.detail.SpawnPool
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import com.cobblemon.mod.common.api.spawning.selection.FlatContextWeightedSelector
import com.cobblemon.mod.common.api.spawning.selection.SpawningSelector
import java.util.concurrent.CompletableFuture
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos

/**
 * A type of spawner that occurs at a single point and provides its own means of generating a
 * context from a particular cause and position. The trigger spawner must be directly called to
 * spawn things (almost as if it was being triggered) as opposed to being scheduled on a ticker.
 *
 * @author Hiroku
 * @since February 3rd, 2024
 */
abstract class TriggerSpawner<T : SpawnCause>(override val name: String, var spawns: SpawnPool) : Spawner {
    private var selector: SpawningSelector = FlatContextWeightedSelector()
    override val influences = mutableListOf<SpawningInfluence>()

    open fun run(cause: T, world: ServerLevel, pos: BlockPos): CompletableFuture<*>? {
        val context = parseContext(cause, world, pos) ?: return null
        selector.select(this, listOf(context))?.let { (_, spawn) ->
            val action = spawn.doSpawn(context)
            action.complete()
            return action.future
        }
        return null
    }

    /** Parses a context, if possible, from the given cause and position. */
    abstract fun parseContext(cause: T, world: ServerLevel, pos: BlockPos): SpawningContext?

    override fun canSpawn() = true
    override fun getSpawningSelector() = selector
    override fun setSpawningSelector(selector: SpawningSelector) { this.selector = selector }
    override fun getSpawnPool() = spawns
    override fun setSpawnPool(spawnPool: SpawnPool) { spawns = spawnPool }
}