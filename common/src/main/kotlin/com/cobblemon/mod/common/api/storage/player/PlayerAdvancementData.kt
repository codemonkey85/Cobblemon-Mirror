/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.player

import com.cobblemon.mod.common.advancement.criterion.AspectCriterion
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.server.level.ServerPlayer
import net.minecraft.resources.ResourceLocation

class PlayerAdvancementData {

    var totalCaptureCount: Int = 0
        private set
    var totalEggsHatched: Int = 0
        private set
    var totalEvolvedCount: Int = 0
        private set
    var totalBattleVictoryCount: Int = 0
        private set
    var totalPvPBattleVictoryCount: Int = 0
        private set
    var totalPvWBattleVictoryCount: Int = 0
        private set
    var totalPvNBattleVictoryCount: Int = 0
        private set
    var totalShinyCaptureCount: Int = 0
        private set
    var totalTradedCount: Int = 0
        private set

    private var totalTypeCaptureCounts = mutableMapOf<String, Int>()
    private var totalDefeatedCounts = mutableMapOf<ResourceLocation, Int>()
    var aspectsCollected = mutableMapOf<ResourceLocation, MutableSet<String>>()
        private set

    fun updateTotalCaptureCount() {
        totalCaptureCount++
    }

    fun updateTotalEggsHatched() {
        totalEggsHatched++
    }

    fun updateTotalEvolvedCount() {
        totalEvolvedCount++
    }

    fun updateTotalBattleVictoryCount() {
        totalBattleVictoryCount++
    }

    fun updateTotalPvPBattleVictoryCount() {
        totalPvPBattleVictoryCount++
    }

    fun updateTotalPvWBattleVictoryCount() {
        totalPvWBattleVictoryCount++
    }

    fun updateTotalPvNBattleVictoryCount() {
        totalPvNBattleVictoryCount++
    }

    fun updateTotalShinyCaptureCount() {
        totalShinyCaptureCount++
    }

    fun updateTotalTradedCount() {
        totalTradedCount++
    }

    fun getTotalTypeCaptureCount(type: ElementalType): Int {
        if (!totalTypeCaptureCounts.containsKey(key = type.name)) {
            totalTypeCaptureCounts[type.name] = 0
        }
        return totalTypeCaptureCounts.get(key = type.name) ?: 0
    }

    fun updateTotalTypeCaptureCount(type: ElementalType) {
        val count = totalTypeCaptureCounts[type.name] ?: 0
        if (count == 0) {
            totalTypeCaptureCounts[type.name] = 1
        } else {
            totalTypeCaptureCounts.replace(type.name, count + 1)
        }
    }

    fun updateTotalDefeatedCount(pokemon: Pokemon) {
        val count = totalDefeatedCounts[pokemon.species.resourceIdentifier] ?: 0
        if (count == 0) {
            totalDefeatedCounts[pokemon.species.resourceIdentifier] = 1
        } else {
            totalDefeatedCounts.replace(pokemon.species.resourceIdentifier, count + 1)
        }
    }

    fun updateAspectsCollected(player: ServerPlayer, pokemon: Pokemon) {
        val aspectConditions = player.advancements.progress.keys
            .flatMap { it.value.criteria.values }
            .mapNotNull { it.trigger }
            .filterIsInstance<AspectCriterion>()

        val trackedAspects = aspectConditions
            .filter { it.pokemon == pokemon.species.resourceIdentifier }
            .flatMap { it.aspects }

        if (trackedAspects.isNotEmpty()) {
            val collectedAspects = aspectsCollected.getOrPut(pokemon.species.resourceIdentifier) { mutableSetOf() }
            pokemon.aspects.filter(trackedAspects::contains).forEach(collectedAspects::add)
        }
    }
}