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

object AttackAngryAtTask {
    fun create(): OneShot<LivingEntity>  = BehaviorBuilder.create {
        it.group(
            it.present(MemoryModuleType.ANGRY_AT),
            it.registered(MemoryModuleType.ATTACK_TARGET)
        ).apply(it) { angryAt, attackTarget ->
            Trigger { world, entity, _ ->
                val angryAt = it.get(angryAt)
                val livingEntity = world.getEntity(angryAt) as? LivingEntity
                if (livingEntity != null) {
                    entity.brain.setMemory(MemoryModuleType.ATTACK_TARGET, livingEntity)
                } else {
                    entity.brain.eraseMemory(MemoryModuleType.ANGRY_AT)
                }
                return@Trigger true
            }
        }
    }
}