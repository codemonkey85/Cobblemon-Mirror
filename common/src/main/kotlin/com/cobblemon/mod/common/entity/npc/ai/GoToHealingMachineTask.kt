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
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.util.getNearbyBlockEntities
import com.cobblemon.mod.common.util.resolveDouble
import com.cobblemon.mod.common.util.resolveFloat
import com.cobblemon.mod.common.util.resolveInt
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BlockPosTracker
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.WalkTarget
import net.minecraft.world.phys.AABB

object GoToHealingMachineTask {
    @JvmStatic
    val runtime = MoLangRuntime().setup()

    fun create(
        horizontalSearchRange: Expression,
        verticalSearchRange: Expression,
        speedMultiplier: Expression,
        completionRange: Expression
    ): OneShot<in LivingEntity> {
        return BehaviorBuilder.create {
            it.group(
                it.absent(MemoryModuleType.WALK_TARGET),
                it.registered(MemoryModuleType.LOOK_TARGET),
                it.absent(CobblemonMemories.NPC_BATTLING)
            ).apply(it) { walkTarget, lookTarget, _ ->
                Trigger { world, entity, time ->
                    if (entity !is NPCEntity) {
                        return@Trigger false
                    }

                    val horizontalSearchRange = runtime.resolveDouble(horizontalSearchRange)
                    val verticalSearchRange = runtime.resolveDouble(verticalSearchRange)
                    val speedMultiplier = runtime.resolveFloat(speedMultiplier)
                    val completionRange = runtime.resolveInt(completionRange)

                    if ((entity.party?.getHealingRemainderPercent() ?: 0F) > 0F) {
                        val npcPos = entity.blockPosition()
                        val nearestFreeHealer = world
                            .getNearbyBlockEntities(
                                box = AABB.ofSize(
                                    entity.position(),
                                    horizontalSearchRange,
                                    verticalSearchRange,
                                    horizontalSearchRange
                                ),
                                blockEntityType = CobblemonBlockEntities.HEALING_MACHINE
                            )
                            .filterNot { it.second.isInUse }
                            .minByOrNull { it.first.distSqr(npcPos) }
                            ?.first
                        if (nearestFreeHealer != null) {
                            walkTarget.set(WalkTarget(nearestFreeHealer, speedMultiplier, completionRange))
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