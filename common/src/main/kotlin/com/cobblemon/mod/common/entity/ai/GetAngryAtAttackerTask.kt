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

object GetAngryAtAttackerTask {
    fun create(): OneShot<in LivingEntity> {
        return BehaviorBuilder.create {
            it.group(
                it.present(MemoryModuleType.HURT_BY_ENTITY),
                it.registered(MemoryModuleType.ANGRY_AT)
            ).apply(it) { hurtByEntity, angryAt ->
                Trigger { _, entity, _ ->
                    val hurtByEntity = it.get(hurtByEntity)
                    val angryAt = it.tryGet(angryAt).orElse(null)
                    if (angryAt != null && angryAt == hurtByEntity.uuid) {
                        return@Trigger false
                    }
                    entity.brain.setMemory(MemoryModuleType.ANGRY_AT, hurtByEntity.uuid)
                    return@Trigger true
                }
            }
        }
    }
}