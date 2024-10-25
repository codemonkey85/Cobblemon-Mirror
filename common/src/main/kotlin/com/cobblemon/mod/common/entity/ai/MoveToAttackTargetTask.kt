/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.ai

import com.bedrockk.molang.Expression
import com.bedrockk.molang.runtime.struct.QueryStruct
import com.cobblemon.mod.common.api.ai.config.task.TaskConfig.Companion.runtime
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.resolveFloat
import com.cobblemon.mod.common.util.resolveInt
import com.cobblemon.mod.common.util.withQueryValue
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.WalkTarget

object MoveToAttackTargetTask {
    fun create(
        speedMultiplier: Expression = "0.5".asExpression(),
        closeEnoughDistance: Expression = "1".asExpression()
    ): OneShot<LivingEntity> = BehaviorBuilder.create {
        it.group(
            it.present(MemoryModuleType.ATTACK_TARGET),
            it.registered(MemoryModuleType.WALK_TARGET)
        ).apply(it) { attackTarget, walkTarget ->
            Trigger { world, entity, _ ->
                runtime.withQueryValue("entity", (entity as? PosableEntity)?.struct ?: QueryStruct(hashMapOf()))
                val speedMultiplier = runtime.resolveFloat(speedMultiplier)
                val closeEnoughDistance = runtime.resolveInt(closeEnoughDistance)

                val attackTarget = it.get(attackTarget)
                val position = attackTarget.position()
                val walkTarget = it.tryGet(walkTarget).orElse(null)
                if (walkTarget == null || walkTarget.target.currentPosition().distanceToSqr(position) > closeEnoughDistance) {
                    entity.brain.setMemory(MemoryModuleType.WALK_TARGET, WalkTarget(attackTarget, speedMultiplier, closeEnoughDistance))
                    true
                } else {
                    false
                }
            }
        }
    }
}