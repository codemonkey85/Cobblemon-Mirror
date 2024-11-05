/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.npc.ai

import com.bedrockk.molang.Expression
import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.block.entity.HealingMachineBlockEntity
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.getNearbyBlockEntities
import com.cobblemon.mod.common.util.resolveDouble
import com.cobblemon.mod.common.util.withQueryValue
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.Behavior
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.minecraft.world.phys.AABB

class HealUsingHealingMachineTask(
    val horizontalUseRange: Expression = "3".asExpression(),
    val verticalUseRange: Expression = "2".asExpression(),
) : Behavior<LivingEntity>(
    mapOf(
        MemoryModuleType.WALK_TARGET to MemoryStatus.VALUE_ABSENT,
        CobblemonMemories.NPC_BATTLING to MemoryStatus.VALUE_ABSENT
    )
) {
    companion object {
        val runtime = MoLangRuntime().setup()
    }

    var blockPos = BlockPos(0, 0, 0)
    var nearestHealer: HealingMachineBlockEntity? = null

    override fun canStillUse(world: ServerLevel, entity: LivingEntity, l: Long): Boolean {
        val healer = nearestHealer
        return !(healer == null || healer.currentUser != entity.uuid || world.getBlockEntity(blockPos) != healer)
    }

    override fun checkExtraStartConditions(world: ServerLevel, entity: LivingEntity): Boolean {
        if (entity !is NPCEntity) {
            return false
        }

        val partyNeedsHealing = (entity.party?.getHealingRemainderPercent() ?: 0.0F) > 0.0F
        if (!partyNeedsHealing) {
            return false
        }
        val npcPos = entity.blockPosition()

        runtime.withQueryValue("entity", entity.struct)
        val horizRange = runtime.resolveDouble(horizontalUseRange)
        val vertRange = runtime.resolveDouble(verticalUseRange)

        world
            .getNearbyBlockEntities(AABB.ofSize(entity.position(), horizRange, vertRange, horizRange), CobblemonBlockEntities.HEALING_MACHINE)
            .filterNot { it.second.isInUse }
            .minByOrNull { it.first.distSqr(npcPos) }
            ?.let {
                blockPos = it.first
                nearestHealer = it.second
            }

        return this.nearestHealer != null
    }

    override fun start(world: ServerLevel, entity: LivingEntity, l: Long) {
        entity as NPCEntity
        nearestHealer?.activate(entity.uuid, entity.party ?: return)
        entity.playAnimation("punch_right")
    }
}