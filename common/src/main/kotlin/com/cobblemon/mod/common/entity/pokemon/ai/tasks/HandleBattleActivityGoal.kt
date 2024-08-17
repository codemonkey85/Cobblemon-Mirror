/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.tasks

import com.cobblemon.mod.common.CobblemonActivities
import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.schedule.Activity

/**
 * Moves Pokémon in and out of the battling activity.
 *
 * @author Hiroku
 * @since April 8th, 2024
 */
object HandleBattleActivityGoal {
    fun create(): OneShot<PokemonEntity> {
        return BehaviorBuilder.create {
            it.group(
                it.registered(CobblemonMemories.POKEMON_BATTLE),
                it.registered(MemoryModuleType.WALK_TARGET),
                it.registered(MemoryModuleType.LOOK_TARGET),
            ).apply(it) { battle, walkTarget, lookTarget ->
                Trigger { world, entity, _ ->
                    val battleUUID = it.tryGet(battle).orElse(null)
                    if (battleUUID != null && !entity.brain.isActive(CobblemonActivities.BATTLING_ACTIVITY)) {
                        entity.brain.setActiveActivityToFirstValid(listOf(CobblemonActivities.BATTLING_ACTIVITY))
                        entity.brain.eraseMemory(MemoryModuleType.WALK_TARGET)
                        entity.brain.eraseMemory(MemoryModuleType.LOOK_TARGET)
                        entity.navigation.stop()
                        return@Trigger true
                    } else if (battleUUID == null && entity.brain.isActive(CobblemonActivities.BATTLING_ACTIVITY)) {
                        entity.brain.setActiveActivityToFirstValid(listOf(Activity.IDLE))
                        entity.brain.eraseMemory(MemoryModuleType.WALK_TARGET)
                        entity.brain.eraseMemory(MemoryModuleType.LOOK_TARGET)
                        return@Trigger true
                    }
                    return@Trigger false
                }
            }
        }
    }
}