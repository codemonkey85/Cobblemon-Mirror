/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.tasks

import java.util.function.Predicate
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.EntityTracker
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.WalkTarget

/**
 * Must find
 *
 * @author Plastered_Crab
 * @since May 20th, 2024
 */
object FindEntityTask {
    fun <T : LivingEntity> create(type: EntityType<out T>, maxDistance: Int, targetModule: MemoryModuleType<T>, speed: Float, completionRange: Int): BehaviorControl<LivingEntity> {
        return create(type, maxDistance, { true }, { true }, targetModule, speed, completionRange)
    }

    fun <E : LivingEntity, T : LivingEntity> create(type: EntityType<out T>, maxDistance: Int, entityPredicate: Predicate<E>, targetPredicate: Predicate<T>, targetModule: MemoryModuleType<T>, speed: Float, completionRange: Int): BehaviorControl<E> {
        val i = maxDistance * maxDistance
        val predicate = Predicate { entity: LivingEntity -> type == entity.type && targetPredicate.test(entity as T) }
        return BehaviorBuilder.create { context: BehaviorBuilder.Instance<E> ->
            context.group(
                context.registered(targetModule),
                context.registered(MemoryModuleType.LOOK_TARGET),
                context.absent(MemoryModuleType.WALK_TARGET),
                context.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
            ).apply<Trigger<E>>(context) { targetValue, lookTarget, walkTarget, visibleMobs ->
                Trigger<E> { world: ServerLevel, entity: E, time: Long ->
                    val livingTargetCache = context.get(visibleMobs)
                    if (entityPredicate.test(entity) && livingTargetCache.contains(predicate)) {
                        val optional = livingTargetCache.findClosest { target: LivingEntity -> target.distanceTo(entity) <= i.toDouble() && predicate.test(target) }
                        optional.ifPresent { target: LivingEntity->
                            targetValue.set(target as T)
                            lookTarget.set(EntityTracker(target, true))
                            walkTarget.set(WalkTarget(EntityTracker(target, false), speed, completionRange))
                        }
                        return@Trigger true
                    } else {
                        return@Trigger false
                    }
                }
            }
        }
    }
}