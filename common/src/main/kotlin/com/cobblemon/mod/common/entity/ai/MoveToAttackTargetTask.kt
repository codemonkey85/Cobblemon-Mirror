/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.ai

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.WalkTarget

object MoveToAttackTargetTask {
    fun create(): OneShot<LivingEntity> = BehaviorBuilder.create {
        it.group(
            it.present(MemoryModuleType.ATTACK_TARGET),
            it.registered(MemoryModuleType.WALK_TARGET)
        ).apply(it) { attackTarget, walkTarget ->
            Trigger { world, entity, _ ->
                val attackTarget = it.get(attackTarget)
                val position = attackTarget.position()
                val walkTarget = it.tryGet(walkTarget).orElse(null)
                if (walkTarget == null || walkTarget.target.currentPosition().distanceToSqr(position) > 4.0) {
                    entity.brain.setMemory(MemoryModuleType.WALK_TARGET, WalkTarget(attackTarget, 0.5F, 1))
                    true
                } else {
                    false
                }
            }
        }
    }
}