/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.tasks

import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.entity.AgeableMob
import net.minecraft.world.entity.ai.behavior.EntityTracker
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.WalkTarget

// modified mojang task for Pokémon species specific behaviour
object WalkTowardsParentSpeciesTask {
    fun create(
        executionRange: UniformInt,
        speed: Float
    ): OneShot<AgeableMob> = BehaviorBuilder.create { context ->
        context.group(
            context.present(MemoryModuleType.NEAREST_VISIBLE_ADULT),
            context.registered(MemoryModuleType.LOOK_TARGET),
            context.absent(MemoryModuleType.WALK_TARGET)
        ).apply(context) { nearestVisibleAdult , lookTarget, walkTarget ->
            Trigger { _, entity, _ ->
                val passiveEntity = context.get(nearestVisibleAdult) as AgeableMob
                if (
                    entity.closerThan(passiveEntity, (executionRange.maxValue + 1).toDouble())
                    && !entity.closerThan(passiveEntity, executionRange.minValue.toDouble())
                ) {
                    val walkTargetX = WalkTarget(EntityTracker(passiveEntity, false), speed, executionRange.minValue - 1)
                    lookTarget.set(EntityTracker(passiveEntity, true))
                    walkTarget.set(walkTargetX)
                    true
                } else {
                    false
                }
            }
        }
    }
}
