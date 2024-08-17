/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.tasks

import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.behavior.EntityTracker
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities


/**
 * Must kill
 *
 * @author Plastered_Crab
 * @since May 20th, 2024
 */
object AttackTask {
    fun create(distance: Int, forwardMovement: Float): OneShot<Mob> {
        return BehaviorBuilder.create { context: BehaviorBuilder.Instance<Mob> ->
            context.group(
                context.absent(MemoryModuleType.WALK_TARGET),
                context.registered(MemoryModuleType.LOOK_TARGET),
                context.present(MemoryModuleType.ATTACK_TARGET),
                context.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
            ).apply(context) { walkTarget, lookTarget, attackTarget, visibleMobs ->
                Trigger { world: ServerLevel, entity: Mob, time: Long ->
                    val livingEntity = context.get(attackTarget) as LivingEntity
                    if (livingEntity.closerThan(entity, distance.toDouble()) && (context.get(visibleMobs) as NearestVisibleLivingEntities).contains(livingEntity)) {
                        lookTarget.set(EntityTracker(livingEntity, true))
                        entity.moveControl.strafe(-forwardMovement, 0.0f)
                        entity.yRot = Mth.clamp(entity.yRot, entity.yHeadRot, 0.0f)
                        return@Trigger true
                    } else {
                        return@Trigger false
                    }
                }
            }
        }
    }
}