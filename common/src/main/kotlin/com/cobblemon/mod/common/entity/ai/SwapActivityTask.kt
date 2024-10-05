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
import net.minecraft.world.entity.schedule.Activity

/**
 * A task that swaps the current activity of the entity to the specified activity if the entity possesses the specified
 * memories or lacks the specified memories.
 *
 * I just get tired of writing the same goal 5 different ways.
 *
 * @author Hiroku
 * @since September 30th, 2024
 */
object SwapActivityTask {
    fun <P1> possessing(
        memory: MemoryModuleType<P1>,
        activity: Activity
    ): OneShot<LivingEntity> {
        return BehaviorBuilder.create { it.group(it.present(memory)).apply(it) { _ -> resetTrigger(activity) } }
    }

    fun <P1, P2> possessing(
        memory1: MemoryModuleType<P1>,
        memory2: MemoryModuleType<P2>,
        activity: Activity
    ): OneShot<LivingEntity> {
        return BehaviorBuilder.create {
            it.group(it.present(memory1), it.present(memory2)).apply(it) { _, _ -> resetTrigger(activity) }
        }
    }

    fun <P1, P2, P3> possessing(
        memory1: MemoryModuleType<P1>,
        memory2: MemoryModuleType<P2>,
        memory3: MemoryModuleType<P3>,
        activity: Activity
    ): OneShot<LivingEntity> {
        return BehaviorBuilder.create {
            it.group(it.present(memory1), it.present(memory2), it.present(memory3)).apply(it) { _, _, _ ->
                resetTrigger(activity)
            }
        }
    }

    fun <P1> lacking(
        memory: MemoryModuleType<P1>,
        activity: Activity
    ): OneShot<LivingEntity> {
        return BehaviorBuilder.create { it.group(it.absent(memory)).apply(it) { _ -> resetTrigger(activity) } }
    }

    fun <P1, P2> lacking(
        memory1: MemoryModuleType<P1>,
        memory2: MemoryModuleType<P2>,
        activity: Activity
    ): OneShot<LivingEntity> {
        return BehaviorBuilder.create {
            it.group(it.absent(memory1), it.absent(memory2)).apply(it) { _, _ -> resetTrigger(activity) }
        }
    }

    fun <P1, P2, P3> lacking(
        memory1: MemoryModuleType<P1>,
        memory2: MemoryModuleType<P2>,
        memory3: MemoryModuleType<P3>,
        activity: Activity
    ): OneShot<LivingEntity> {
        return BehaviorBuilder.create {
            it.group(it.absent(memory1), it.absent(memory2), it.absent(memory3)).apply(it) { _, _, _ ->
                resetTrigger(activity)
            }
        }
    }

    fun resetTrigger(activity: Activity) = Trigger<LivingEntity> { _, entity, _ ->
        entity.brain.setActiveActivityIfPossible(activity)
        return@Trigger true
    }
}