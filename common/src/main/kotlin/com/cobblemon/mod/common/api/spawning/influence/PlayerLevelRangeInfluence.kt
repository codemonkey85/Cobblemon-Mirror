/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.influence

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.Cobblemon.config
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnAction
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction
import com.cobblemon.mod.common.util.math.intersection
import kotlin.math.max
import kotlin.math.min
import net.minecraft.server.level.ServerPlayer

/**
 * A [SpawningInfluence] that tends spawns around a player to be within their level range.
 * It will adjust the possible level range to be the intersection of the acceptable level range
 * and the player's level range. For situations where there is no intersection, the bottom or
 * top half of the Pokémon's range is used.
 *
 * @author Hiroku
 * @since February 14th, 2022
 */
open class PlayerLevelRangeInfluence(
    player: ServerPlayer,
    val variation: Int,
    val noPokemonRange: IntRange = 1 .. config.minimumLevelRangeMax,
    val recalculationMillis: Long = 5000L
) : SpawningInfluence {
    val uuid = player.uuid
    var lastCalculatedTime: Long = 0
    var previousRange: IntRange = noPokemonRange

    fun getPlayerLevelRange(): IntRange {
        return if (System.currentTimeMillis() - lastCalculatedTime > recalculationMillis) {
            lastCalculatedTime = System.currentTimeMillis()

            val party = Cobblemon.storage.getParty(uuid)
            previousRange = if (party.any()) {
                //val minimumLevel = party.minOf { it.level }
                val maximumLevel = party.maxOf { it.level }
                IntRange(max(maximumLevel - variation, 1), min(config.maxPokemonLevel, max(maximumLevel + variation, config.minimumLevelRangeMax)))
            } else {
                noPokemonRange
            }
            previousRange
        } else {
            previousRange
        }
    }

    override fun affectAction(action: SpawnAction<*>) {
        if (action is PokemonSpawnAction && action.props.level == null) {
            val playerLevelRange = getPlayerLevelRange()
            val derivedLevelRange = action.detail.getDerivedLevelRange()
            var spawnLevelRange = playerLevelRange.intersection(derivedLevelRange)
            val pokemonRangeWidth = derivedLevelRange.last - derivedLevelRange.first
            if (spawnLevelRange.isEmpty()){
                spawnLevelRange = if (derivedLevelRange.first > playerLevelRange.last) {
                    derivedLevelRange.first..(derivedLevelRange.first + pokemonRangeWidth / 4F).toInt()
                }
                else {
                    (derivedLevelRange.first + 3 * pokemonRangeWidth / 4F).toInt()..derivedLevelRange.last
                }
            }
            action.props.level = spawnLevelRange.random()
        }
    }
}