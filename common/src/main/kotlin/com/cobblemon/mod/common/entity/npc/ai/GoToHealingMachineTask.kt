/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.npc.ai

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.util.getNearbyBlockEntities
import net.minecraft.world.entity.ai.behavior.BlockPosTracker
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.WalkTarget
import net.minecraft.world.phys.AABB

object GoToHealingMachineTask {
    fun create(horizontalRange: Int, verticalRange: Int, walkSpeed: Float, completionRange: Int): OneShot<NPCEntity> {
        return BehaviorBuilder.create {
            it.group(
                it.absent(MemoryModuleType.WALK_TARGET),
                it.registered(MemoryModuleType.LOOK_TARGET),
                it.absent(CobblemonMemories.NPC_BATTLING)
            ).apply(it) { walkTarget, lookTarget, _ ->
                Trigger { world, entity, time ->
                    if ((entity.staticParty?.getHealingRemainderPercent() ?: 0F) > 0F) {
                        val npcPos = entity.blockPosition()
                        val nearestFreeHealer = world
                            .getNearbyBlockEntities(AABB.ofSize(entity.position(), horizontalRange.toDouble(), verticalRange.toDouble(), horizontalRange.toDouble()), CobblemonBlockEntities.HEALING_MACHINE)
                            .filterNot { it.second.isInUse }
                            .minByOrNull { it.first.distSqr(npcPos) }
                            ?.first
                        if (nearestFreeHealer != null) {
                            walkTarget.set(WalkTarget(nearestFreeHealer, walkSpeed, completionRange))
                            lookTarget.set(BlockPosTracker(nearestFreeHealer))
                            return@Trigger true
                        }
                    }
                    return@Trigger false
                }
            }
        }
    }
}