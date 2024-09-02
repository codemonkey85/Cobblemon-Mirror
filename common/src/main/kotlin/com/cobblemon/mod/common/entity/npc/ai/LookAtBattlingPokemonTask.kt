/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.npc.ai

import com.cobblemon.mod.common.CobblemonMemories
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.EntityTracker
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType

object LookAtBattlingPokemonTask {
    fun create(minDurationTicks: Int = 60, maxDurationTicks: Int = 100): OneShot<LivingEntity> {
        return BehaviorBuilder.create {
            it.group(
                it.absent(MemoryModuleType.LOOK_TARGET),
                it.present(CobblemonMemories.BATTLING_POKEMON)
            ).apply(it) { lookTarget, visibleMobs ->
                Trigger { world, _, _ ->
                    val lookEntity = it.get(visibleMobs).randomOrNull()?.let(world::getEntity)
                    if (lookEntity == null) {
                        return@Trigger false
                    } else {
                        lookTarget.setWithExpiry(EntityTracker(lookEntity, true), (minDurationTicks..maxDurationTicks).random().toLong())
                        return@Trigger true
                    }
                }
            }
        }
    }
}