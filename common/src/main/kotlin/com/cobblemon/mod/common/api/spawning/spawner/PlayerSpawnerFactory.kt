/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.spawner

import com.cobblemon.mod.common.api.spawning.CobblemonSpawnPools
import com.cobblemon.mod.common.api.spawning.CobblemonSpawnRules
import com.cobblemon.mod.common.api.spawning.SpawnerManager
import com.cobblemon.mod.common.api.spawning.detail.SpawnPool
import com.cobblemon.mod.common.api.spawning.influence.PlayerLevelRangeInfluence
import com.cobblemon.mod.common.api.spawning.influence.RestrictedSpawnBlocksInfluence
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import com.cobblemon.mod.common.api.spawning.rules.SpawnRule
import com.cobblemon.mod.common.util.mutableLazy
import net.minecraft.server.level.ServerPlayer

/**
 * Responsible for creating [PlayerSpawner]s with whatever appropriate settings. You can
 * swap over the spawn pool and the influences here, and it will apply to all newly-generated
 * [PlayerSpawner]s.
 *
 * @author Hiroku
 * @since February 14th, 2022
 */
object PlayerSpawnerFactory {
    var spawns: SpawnPool by mutableLazy {
        CobblemonSpawnPools.WORLD_SPAWN_POOL
    }

    /**
     * The list of influences for a new [PlayerSpawner].
     * The influences will be added to the spawner in the order they are added to this list.
     *
     * [PlayerLevelRangeInfluence] adjusts the level range of Pokémon that can spawn based on the player's level.
     * [RestrictedSpawnBlocksInfluence] prevents Pokémon from spawning on certain blocks.
     */
    var influenceBuilders = mutableListOf<(player: ServerPlayer) -> SpawningInfluence?>({
        PlayerLevelRangeInfluence(
            it,
            variation = 5
        )
    }, { RestrictedSpawnBlocksInfluence() })

    fun create(spawnerManager: SpawnerManager, player: ServerPlayer): PlayerSpawner {
        val influences = influenceBuilders.mapNotNull { it(player) }
        return PlayerSpawner(player, spawns, spawnerManager).also {
            it.influences.addAll(influences)
            it.influences.addAll(
                CobblemonSpawnRules.rules.values.filter(SpawnRule::enabled).flatMap(SpawnRule::components)
            )
        }
    }
}
