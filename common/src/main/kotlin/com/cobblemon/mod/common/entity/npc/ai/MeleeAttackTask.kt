/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.npc.ai

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType

object MeleeAttackTask {
    fun create(range: Float, cooldownTicks: Long): OneShot<LivingEntity> = BehaviorBuilder.create {
        it.group(
            it.present(MemoryModuleType.ATTACK_TARGET),
            it.registered(MemoryModuleType.ATTACK_COOLING_DOWN)
        ).apply(it) { attackTarget, cooldown ->
            Trigger { world, entity, _ ->
                val attackTarget = it.get(attackTarget)
                if (entity.distanceTo(attackTarget) <= range) {
                    entity.doHurtTarget(attackTarget)
                    cooldown.setWithExpiry(true, cooldownTicks)
                    true
                } else {
                    false
                }
            }
        }
    }
}