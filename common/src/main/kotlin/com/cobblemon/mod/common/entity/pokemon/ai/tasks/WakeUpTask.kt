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
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType

object WakeUpTask {
    fun create(): OneShot<PokemonEntity> {
        return BehaviorBuilder.create {
            it.group(
                it.absent(CobblemonMemories.POKEMON_DROWSY),
                it.registered(MemoryModuleType.HURT_BY),
                it.registered(MemoryModuleType.HURT_BY_ENTITY),
                it.registered(MemoryModuleType.ANGRY_AT),
            ).apply(it) { _, hurtBy, hurtByEntity, angerTarget ->
                Trigger { world, entity, _ ->
                    if (entity.pokemon.status?.status == Statuses.SLEEP && entity.pokemon.storeCoordinates.get()?.store !is PartyStore) {
                        entity.pokemon.status = null
                        entity.brain.eraseMemory(CobblemonMemories.POKEMON_SLEEPING)
                        entity.brain.useDefaultActivity()
                        return@Trigger true
                    } else {
                        return@Trigger false
                    }
                }
            }
        }
    }
}