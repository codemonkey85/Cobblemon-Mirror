/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.bedrockk.molang.runtime.struct.QueryStruct
import com.cobblemon.mod.common.api.ai.BrainConfigurationContext
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.resolveBoolean
import com.cobblemon.mod.common.util.toDF
import com.cobblemon.mod.common.util.withQueryValue
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.DoNothing
import net.minecraft.world.entity.ai.behavior.RunOne

/**
 * A task where each time the brain is ticked, a different task might run. This differs from [RandomTaskConfig] in that
 * the same entity can run all of the different possibilities, rather than [RandomTaskConfig] where once the entity
 * spawns, it will only use whichever was selected at the time of spawning.
 *
 * This is most useful for making a single entity randomly choose what to do at a given moment.
 *
 * @author Hiroku
 * @since October 19th, 2024
 */
class OneOfTaskConfig : SingleTaskConfig {
    class OneOfTaskOption {
        val weight: Int = 1
        val task: TaskConfig = SingleTaskConfig { _, _ -> DoNothing(0, 1) }
    }

    val condition: ExpressionLike = "true".asExpressionLike()
    val options = mutableListOf<OneOfTaskOption>()

    override fun createTask(entity: LivingEntity, brainConfigurationContext: BrainConfigurationContext): BehaviorControl<in LivingEntity>? {
        runtime.withQueryValue("entity", (entity as? PosableEntity)?.struct ?: QueryStruct(hashMapOf()))
        if (!runtime.resolveBoolean(condition)) return null
        return RunOne(options.map { it.task.createTasks(entity, brainConfigurationContext).first() toDF it.weight })
    }
}