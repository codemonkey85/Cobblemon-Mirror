/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.spawner

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.spawning.BestSpawner
import com.cobblemon.mod.common.api.spawning.SpawnBucket
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction
import com.cobblemon.mod.common.api.spawning.detail.SpawnPool
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import com.cobblemon.mod.common.api.spawning.selection.SpawningSelector
import java.util.*

/**
 * Interface representing something that performs the action of spawning. Various functions
 * exist to streamline the process of using the [BestSpawner].
 *
 * @author Hiroku
 * @since January 24th, 2022
 */
interface Spawner {
    companion object {
        // var worker = Executors.newSingleThreadExecutor { r -> Thread(r, "Spawning Worker") }
    }

    val name: String
    val influences: MutableList<SpawningInfluence>
    fun getSpawningSelector(): SpawningSelector
    fun setSpawningSelector(selector: SpawningSelector)
    fun getSpawnPool(): SpawnPool
    fun setSpawnPool(spawnPool: SpawnPool)
    fun <R> afterSpawn(action: SpawnAction<R>, result: R) {}
    fun canSpawn(): Boolean
    fun getMatchingSpawns(ctx: SpawningContext) = getSpawnPool().retrieve(ctx).filter { it.isSatisfiedBy(ctx) }
    fun copyInfluences() = influences.filter { !it.isExpired() }.toMutableList()
    fun chooseBucket(): SpawnBucket {
        val buckets = Cobblemon.bestSpawner.config.buckets
        val influences = this.copyInfluences()
        val weightMap = mutableMapOf<SpawnBucket, Float>()

        for (bucket in buckets) {
            var weight = bucket.weight
            for (influence in influences) {
                weight = influence.affectBucketWeight(bucket, weight)
            }
            weightMap[bucket] = weight
        }

        val weightSum = weightMap.values.sum()

        // Make the 0 exclusive and the weightSum inclusive on the random
        val chosenSum = weightSum - Random().nextFloat(weightSum)
        var sum = 0F
        for (bucket in buckets) {
            sum += weightMap[bucket]!!
            if (sum >= chosenSum) {
                return bucket
            }
        }
        return buckets.first()
    }
}