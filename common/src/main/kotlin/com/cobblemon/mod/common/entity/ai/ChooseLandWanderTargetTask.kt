/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.ai

import com.cobblemon.mod.common.CobblemonMemories
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.behavior.BlockPosTracker
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.WalkTarget
import net.minecraft.world.entity.ai.util.LandRandomPos

object ChooseLandWanderTargetTask {
    fun create(chance: Int, horizontalRange: Int, verticalRange: Int, walkSpeed: Float, completionRange: Int): OneShot<PathfinderMob> {
        return BehaviorBuilder.create {
            it.group(
                it.absent(MemoryModuleType.WALK_TARGET),
                it.registered(MemoryModuleType.LOOK_TARGET),
                it.absent(CobblemonMemories.POKEMON_SLEEPING),
            ).apply(it) { walkTarget, lookTarget, isSleeping ->
                Trigger { world, entity, time ->
                    if (world.random.nextInt(chance) != 0) return@Trigger false
                    val targetVec = LandRandomPos.getPos(entity, horizontalRange, verticalRange) ?: return@Trigger false
                    walkTarget.set(WalkTarget(targetVec, walkSpeed, completionRange))
                    lookTarget.set(BlockPosTracker(targetVec.add(0.0, 1.5, 0.0)))
                    return@Trigger true
                }
            }
        }
    }
}
