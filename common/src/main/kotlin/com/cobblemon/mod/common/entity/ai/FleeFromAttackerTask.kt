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

object FleeFromAttackerTask {
    fun create(): OneShot<LivingEntity> {
        return BehaviorBuilder.create {
            it.group(
                it.present(MemoryModuleType.HURT_BY_ENTITY),
                it.registered(MemoryModuleType.AVOID_TARGET)
            ).apply(it) { hurtByEntity, avoidTarget ->
                Trigger { _, entity, _ ->
                    val hurtByEntity = it.get(hurtByEntity)
                    val avoidTarget = it.tryGet(avoidTarget).orElse(null)
                    if (avoidTarget != null && avoidTarget == hurtByEntity.uuid) {
                        return@Trigger false
                    }
                    entity.brain.setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, hurtByEntity, 30 * 20)
                    return@Trigger true
                }
            }
        }
    }
}