/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.bedrockk.molang.Expression
import com.bedrockk.molang.runtime.struct.QueryStruct
import com.cobblemon.mod.common.api.ai.BrainConfigurationContext
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.entity.npc.ai.GoToHealingMachineTask
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.resolveBoolean
import com.cobblemon.mod.common.util.withQueryValue
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl

class GoToHealingMachineTaskConfig(
    val condition: ExpressionLike = "true".asExpressionLike(),
    val horizontalSearchRange: Expression = "10".asExpression(),
    val verticalSearchRange: Expression = "5".asExpression(),
    val speedMultiplier: Expression = "0.33".asExpression(),
    val completionRange: Expression = "1".asExpression()
) : SingleTaskConfig {
    override fun createTask(
        entity: LivingEntity,
        brainConfigurationContext: BrainConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        runtime.withQueryValue("entity", (entity as? PosableEntity)?.struct ?: QueryStruct(hashMapOf()))
        if (!runtime.resolveBoolean(condition)) return null

        return GoToHealingMachineTask.create(
            horizontalSearchRange = horizontalSearchRange,
            verticalSearchRange = verticalSearchRange,
            speedMultiplier = speedMultiplier,
            completionRange = completionRange
        )
    }

}