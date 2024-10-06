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
import com.cobblemon.mod.common.block.entity.HealingMachineBlockEntity
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.util.getNearbyBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.ai.behavior.Behavior
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.minecraft.world.phys.AABB

class HealUsingHealingMachineTask : Behavior<NPCEntity>(
    mapOf(
        MemoryModuleType.WALK_TARGET to MemoryStatus.VALUE_ABSENT,
        CobblemonMemories.NPC_BATTLING to MemoryStatus.VALUE_ABSENT
    )
) {
    var blockPos = BlockPos(0, 0, 0)
    var nearestHealer: HealingMachineBlockEntity? = null

    override fun canStillUse(world: ServerLevel, entity: NPCEntity, l: Long): Boolean {
        val healer = nearestHealer
        return !(healer == null || healer.currentUser != entity.uuid || world.getBlockEntity(blockPos) != healer)
    }

    override fun checkExtraStartConditions(world: ServerLevel, entity: NPCEntity): Boolean {
        val partyNeedsHealing = (entity.staticParty?.getHealingRemainderPercent() ?: 0.0F) > 0.0F
        if (!partyNeedsHealing) {
            return false
        }
        val npcPos = entity.blockPosition()
        world
            .getNearbyBlockEntities(AABB.ofSize(entity.position(), 3.0, 2.0, 3.0), CobblemonBlockEntities.HEALING_MACHINE)
            .filterNot { it.second.isInUse }
            .minByOrNull { it.first.distSqr(npcPos) }
            ?.let {
                blockPos = it.first
                nearestHealer = it.second
            }

        return this.nearestHealer != null
    }

    override fun start(world: ServerLevel, entity: NPCEntity, l: Long) {
        nearestHealer?.activate(entity.uuid, entity.staticParty ?: return)
        entity.playAnimation("punch_right")
    }
}