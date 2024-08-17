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
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType

/**
 * When NPC battling memory is added, this task swaps the NPC's activity over to npc_battling
 *
 * @author Hiroku
 * @since February 24th, 2024
 */
object SwitchToBattleTask {
    fun create(): OneShot<LivingEntity> {
        return BehaviorBuilder.create {
            it.group(
                it.registered(MemoryModuleType.WALK_TARGET),
                it.present(CobblemonMemories.NPC_BATTLING)
            ).apply(it) { walkTarget, _ ->
                Trigger { _, entity, _ ->
                    walkTarget.erase()
//                    entity.brain.doExclusively(NPCEntity.BATTLING)
                    true
                }
            }
        }
    }
}