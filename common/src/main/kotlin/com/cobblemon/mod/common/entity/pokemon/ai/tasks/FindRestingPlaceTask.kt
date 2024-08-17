/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.tasks

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.api.storage.party.PartyStore
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.getBlockPositions
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.WalkTarget
import net.minecraft.world.phys.AABB

object FindRestingPlaceTask {
    fun create(horizontalSearchDistance: Int, verticalSearchDistance: Int): OneShot<PokemonEntity> {
        return BehaviorBuilder.create {
            it.group(
                it.absent(MemoryModuleType.ANGRY_AT),
                it.absent(MemoryModuleType.ATTACK_TARGET),
                it.absent(MemoryModuleType.WALK_TARGET),
                it.absent(CobblemonMemories.POKEMON_BATTLE),
                it.present(CobblemonMemories.POKEMON_DROWSY),
                it.absent(CobblemonMemories.REST_PATH_COOLDOWN),
                it.absent(CobblemonMemories.POKEMON_SLEEPING),
            ).apply(it) { _, _, walkTarget, _, pokemonDrowsy, restPathCooldown, _ ->
                Trigger { world, entity, _ ->
                    return@Trigger if (it.get(pokemonDrowsy) && entity.pokemon.status?.status != Statuses.SLEEP && entity.pokemon.storeCoordinates.get()?.store !is PartyStore) {
                        entity.brain.setMemoryWithExpiry(CobblemonMemories.REST_PATH_COOLDOWN, true, 40)
                        val position = entity.level()
                            .getBlockPositions(AABB.ofSize(entity.position(), horizontalSearchDistance.toDouble(), verticalSearchDistance.toDouble(), horizontalSearchDistance.toDouble()))
                            .filter(entity::canSleepAt)
//                            .randomOrNull()
                            .minByOrNull { it.distToCenterSqr(entity.position()) }
                        if (position != null) {
                            walkTarget.set(WalkTarget(position.below(), 0.3F, 1))
                        }
                        true
                    } else {
                        false
                    }
                }
            }
        }
    }
}