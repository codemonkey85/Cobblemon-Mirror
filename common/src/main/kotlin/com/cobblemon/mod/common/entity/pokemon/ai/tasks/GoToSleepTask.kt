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
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.api.storage.party.PartyStore
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.status.PersistentStatusContainer
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType

object GoToSleepTask {
    fun create(): OneShot<PokemonEntity> {
        return BehaviorBuilder.create {
            it.group(
                it.absent(MemoryModuleType.ANGRY_AT),
                it.absent(MemoryModuleType.ATTACK_TARGET),
                it.absent(MemoryModuleType.WALK_TARGET),
                it.absent(CobblemonMemories.POKEMON_BATTLE),
                it.registered(CobblemonMemories.POKEMON_DROWSY)
            ).apply(it) { _, _, _, _, pokemonDrowsy ->
                Trigger { world, entity, _ ->
                    val hasSleepStatus = entity.pokemon.status?.status === Statuses.SLEEP
                    if (entity.behaviour.resting.canSleep && ((it.tryGet(pokemonDrowsy).orElse(false) && entity.canSleepAt(entity.blockPosition().below())) || hasSleepStatus) && entity.pokemon.storeCoordinates.get()?.store !is PartyStore) {
                        if (!hasSleepStatus) {
                            entity.pokemon.status = PersistentStatusContainer(Statuses.SLEEP)
                        }
                        entity.brain.setActiveActivityToFirstValid(listOf(CobblemonActivities.POKEMON_SLEEPING_ACTIVITY))
                        entity.brain.setMemory(CobblemonMemories.POKEMON_SLEEPING, true)
                        return@Trigger true
                    } else {
                        return@Trigger false
                    }
                }
            }
        }
    }
}