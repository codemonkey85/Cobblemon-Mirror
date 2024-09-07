/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.detail

import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.spawning.SpawnLoader
import com.cobblemon.mod.common.api.spawning.SpawnSet
import com.cobblemon.mod.common.api.spawning.condition.PrecalculationResult
import com.cobblemon.mod.common.api.spawning.condition.RootPrecalculation
import com.cobblemon.mod.common.api.spawning.condition.SpawningPrecalculation
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import com.cobblemon.mod.common.api.spawning.spawner.Spawner
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType

/**
 * A collection of [SpawnDetail]s with precalculation logic for optimization of searches.
 *
 * A single spawn pool may be used for many different [Spawner]s. Note that changing the
 * [details] list will change the spawns for any [Spawner] sharing this pool. If you want
 * to make a change for a pool specifically to one spawner, take a copy of the pool using
 * [copy], and change that spawner's pool using [Spawner.setSpawnPool].
 *
 * @author Hiroku
 * @since February 9th, 2022
 */
class SpawnPool(val name: String) : JsonDataRegistry<SpawnSet>, Iterable<SpawnDetail> {
    override val id = cobblemonResource("spawn_pool_$name")
    override val type = PackType.SERVER_DATA
    override val observable = SimpleObservable<SpawnPool>()
    override val gson: Gson = SpawnLoader.gson
    override val typeToken = TypeToken.get(SpawnSet::class.java)
    override val resourcePath = id.path
    override fun sync(player: ServerPlayer) {}
    override fun reload(data: Map<ResourceLocation, SpawnSet>) {
        details.clear()
        for (set in data.values) {
            details.addAll(set.filter { it.isValid() })
        }
        precalculate()
    }

    val details = mutableListOf<SpawnDetail>()
    var precalculation: PrecalculationResult<*> = RootPrecalculation.generate(details, emptyList())
    val precalculators = mutableListOf<SpawningPrecalculation<*>>()
//    /** A set of all [RegisteredSpawningContext]s that are mentioned in this pool. */
//    val contexts = mutableSetOf<RegisteredSpawningContext<*>>()

    override fun iterator() = details.iterator()


    fun addPrecalculators(vararg precalculators: SpawningPrecalculation<*>): SpawnPool {
        this.precalculators.addAll(precalculators)
        precalculate()
        return this
    }

    /**
     * Precalculates spawns into hash mappings using the [precalculators] included
     * in this pool as well as the range of contexts mentioned in the pool. This
     * will speed up retrieval later, and thins the herd of spawns that need to be
     * thoroughly examined when a spawn is occurring. This function will probably
     * be slow, especially if there are many precalculators and spawns.
     */
    fun precalculate() {
        if (precalculators.isEmpty()) {
            precalculation = RootPrecalculation.generate(details, emptyList())
        } else {
            precalculation = precalculators.first().generate(details, precalculators.subList(1, precalculators.size))
        }

//        contexts.clear()
//        details.forEach { contexts.add(it.context) }
    }

    /**
     * Retrieves the spawns that are precalculated as being potentially spawns at
     * this context. This, at most, prunes some spawns that were definitely not
     * possible here. The returned list can and almost certainly will include more
     * spawns that are not possible for this context - this function is simple
     * to leverage the precalculation to get a smaller list of spawns as quickly
     * as possible.
     */
    fun retrieve(ctx: SpawningContext): List<SpawnDetail> {
        return precalculation.retrieve(ctx)
    }

    /**
     * Creates a de-referenced copy of the pool which can be modified safely without
     * this pool being changed.
     */
    fun copy(newName: String): SpawnPool {
        val copy = SpawnPool(newName)
        copy.details.addAll(details)
        copy.precalculators.addAll(precalculators)
        copy.precalculation = precalculation
        return copy
    }
}