/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.bedrockk.molang.Expression
import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.struct.QueryStruct
import com.cobblemon.mod.common.api.ai.BrainConfigurationContext
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.resolveBoolean
import com.cobblemon.mod.common.util.resolveDouble
import com.cobblemon.mod.common.util.resolveFloat
import com.cobblemon.mod.common.util.resolveInt
import com.cobblemon.mod.common.util.withQueryValue
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.BlockPosTracker
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.WalkTarget
import net.minecraft.world.entity.ai.util.LandRandomPos

class WanderTaskConfig : SingleTaskConfig {
    val condition: ExpressionLike = "true".asExpressionLike()
    val shouldWander: ExpressionLike = "math.random(0, 20 * 8) <= 1".asExpressionLike()
    val horizontalRange: Expression = "10".asExpression()
    val verticalRange: Expression = "5".asExpression()
    val speedMultiplier: Expression = "0.35".asExpression()
    val completionRange: Expression = "1".asExpression()
    val lookTargetYOffset: Expression = "1.5".asExpression()

    override fun createTask(
        entity: LivingEntity,
        brainConfigurationContext: BrainConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        runtime.withQueryValue("entity", (entity as? PosableEntity)?.struct ?: QueryStruct(hashMapOf()))
        if (!runtime.resolveBoolean(condition)) return null

        return BehaviorBuilder.create {
            it.group(
                it.absent(MemoryModuleType.WALK_TARGET),
                it.registered(MemoryModuleType.LOOK_TARGET)
            ).apply(it) { walkTarget, lookTarget ->
                Trigger { world, entity, time ->
                    if (entity !is PathfinderMob) {
                        return@Trigger false
                    }

                    runtime.withQueryValue("entity", (entity as? PosableEntity)?.struct ?: QueryStruct(hashMapOf()))
                    if (!runtime.resolveBoolean(shouldWander)) return@Trigger false

                    val targetVec = LandRandomPos.getPos(entity, runtime.resolveInt(horizontalRange), runtime.resolveInt(verticalRange)) ?: return@Trigger false
                    walkTarget.set(WalkTarget(targetVec, runtime.resolveFloat(speedMultiplier), runtime.resolveInt(completionRange)))
                    lookTarget.set(BlockPosTracker(targetVec.add(0.0, runtime.resolveDouble(lookTargetYOffset), 0.0)))
                    return@Trigger true
                }
            }
        }
    }
}