/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.spawner

import com.cobblemon.mod.common.api.spawning.SpawnCause
import com.cobblemon.mod.common.api.spawning.SpawnerManager
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import com.cobblemon.mod.common.api.spawning.detail.EntitySpawnResult
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.detail.SpawnPool
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import com.cobblemon.mod.common.api.spawning.selection.FlatContextWeightedSelector
import com.cobblemon.mod.common.api.spawning.selection.SpawningSelector
import net.minecraft.world.entity.Entity

/**
 * A spawner that regularly attempts spawning entities. It has timing utilities,
 * and subclasses must provide the logic for generating a spawn which is called
 * periodically by the server.
 *
 * @author Hiroku
 * @since February 5th, 2022
 */
abstract class TickingSpawner(
    override val name: String,
    var spawns: SpawnPool,
    val manager: SpawnerManager
) : Spawner {
    private var selector: SpawningSelector = FlatContextWeightedSelector()
    override val influences = mutableListOf<SpawningInfluence>()

    override fun canSpawn() = active
    override fun getSpawningSelector() = selector
    override fun setSpawningSelector(selector: SpawningSelector) { this.selector = selector }
    override fun getSpawnPool() = spawns
    override fun setSpawnPool(spawnPool: SpawnPool) { spawns = spawnPool }

    abstract fun run(cause: SpawnCause): Pair<SpawningContext, SpawnDetail>?

    var active = true
    val spawnedEntities = mutableListOf<Entity>()

    var lastSpawnTime = 0L
    var ticksUntilNextSpawn = 100F
    abstract var ticksBetweenSpawns: Float
    var tickTimerMultiplier = 1F

    var removalCheckTicks = 0

    open fun tick() {
        removalCheckTicks++
        influences.removeIf { it.isExpired() }
        if (removalCheckTicks == 60) {
            spawnedEntities.removeIf { it.isRemoved }
            removalCheckTicks = 0
        }

        if (!active) {
            return
        }

        ticksUntilNextSpawn -= tickTimerMultiplier
        if (ticksUntilNextSpawn <= 0) {
            val spawn = run(SpawnCause(spawner = this, bucket = chooseBucket(), entity = getCauseEntity()))
            ticksUntilNextSpawn = ticksBetweenSpawns
            if (spawn != null) {
                val ctx = spawn.first
                val detail = spawn.second
                val spawnAction = detail.doSpawn(ctx = ctx)
                spawnAction.complete()
            }
        }
    }

    override fun <R> afterSpawn(action: SpawnAction<R>, result: R) {
        super.afterSpawn(action, result)
        if (result is EntitySpawnResult) {
            spawnedEntities.addAll(result.entities)
        }
        lastSpawnTime = System.currentTimeMillis()
    }

    open fun getCauseEntity(): Entity? = null

    fun getAllInfluences() = this.influences + manager.influences

    override fun copyInfluences() = this.getAllInfluences().toMutableList()
}
