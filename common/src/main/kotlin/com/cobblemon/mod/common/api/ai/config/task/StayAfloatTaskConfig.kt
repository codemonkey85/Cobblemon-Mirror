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
import com.cobblemon.mod.common.api.ai.WrapperLivingEntityTask
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.entity.ai.StayAfloatTask
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.resolveBoolean
import com.cobblemon.mod.common.util.resolveFloat
import com.cobblemon.mod.common.util.withQueryValue
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.behavior.BehaviorControl

class StayAfloatTaskConfig : SingleTaskConfig {
    val condition: ExpressionLike = "true".asExpressionLike()
    val chance: Expression = "0.01".asExpression()
    override fun createTask(entity: LivingEntity, brainConfigurationContext: BrainConfigurationContext): BehaviorControl<LivingEntity>? {
        runtime.withQueryValue("entity", (entity as? PosableEntity)?.struct ?: QueryStruct(hashMapOf()))
        if (!runtime.resolveBoolean(condition)) return null
        val chance = runtime.resolveFloat(chance)
        return WrapperLivingEntityTask(
            StayAfloatTask(chance),
            PathfinderMob::class.java
        )
    }
}