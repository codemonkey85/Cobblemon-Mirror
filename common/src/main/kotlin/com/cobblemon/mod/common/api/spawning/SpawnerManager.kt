/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import com.cobblemon.mod.common.api.spawning.spawner.Spawner
import com.cobblemon.mod.common.api.spawning.spawner.TickingSpawner
import com.cobblemon.mod.common.util.server
import com.cobblemon.mod.common.world.gamerules.CobblemonGameRules.DO_POKEMON_SPAWNING

/**
 * A manager of various spawners. This is a class in which you should register
 * ticking spawners so that they can be automatically ticked by the server.
 * This also supports registering [SpawningInfluence]s to apply to each spawner
 * that is registered.
 *
 * @author Hiroku
 * @since February 5th, 2022
 */
open class SpawnerManager {
    val spawners = mutableListOf<Spawner>()
        get() {
            if (server()?.isSameThread == false) {
                Cobblemon.LOGGER.error("Illegal access to spawners list from non-server thread!")
                Exception().printStackTrace()
            }
            return field
        }
    val influences = mutableListOf<SpawningInfluence>()
        get() {
            if (server()?.isSameThread == false) {
                Cobblemon.LOGGER.error("Illegal access to influences from non-server thread!")
                Exception().printStackTrace()
            }
            return field
        }
    inline fun <reified T : Spawner> getSpawnersOfType() = spawners.filterIsInstance<T>()
    open fun getSpawnerByName(name: String) = spawners.find { it.name == name }

    open fun registerSpawner(spawner: Spawner) {
        spawners.add(spawner)
        if (spawner !is TickingSpawner) {
            spawner.influences.addAll(influences)
        }
    }

    open fun unregisterSpawner(spawner: Spawner) {
        spawners.remove(spawner)
        if (spawner !is TickingSpawner) {
            spawner.influences.removeAll(influences)
        }
    }

    open fun onServerStarted() {
        spawners.clear()
    }

    open fun onServerTick() {
        // Disables spawning
        if (!Cobblemon.config.enableSpawning || server()?.gameRules?.getBoolean(DO_POKEMON_SPAWNING) == false) {
            return
        }
        influences.removeIf { it.isExpired() }
        getSpawnersOfType<TickingSpawner>().forEach(TickingSpawner::tick)
    }
}