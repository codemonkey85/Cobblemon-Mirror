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
import com.cobblemon.mod.common.util.weightedSelection
import com.cobblemon.mod.common.util.withQueryValue
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.DoNothing

/**
 * Randomly chooses one of the possible tasks to add to the brain. This differs from [OneOfTaskConfig] in that
 * the randomization happens before the brain is created, rather than [OneOfTaskConfig] where the randomization
 * happens each time the brain is ticked.
 *
 * This is most useful when the goal is to apply something like a specialization to an entity where it varies
 * but once put on the entity, it sticks.
 *
 * @author Hiroku
 * @since October 19th, 2024
 */
class RandomTaskConfig : TaskConfig {
    class RandomTaskChoice {
        val weight = 1.0
        val task: TaskConfig = SingleTaskConfig { _, _ -> DoNothing(0, 1) }
    }

    val condition: ExpressionLike = "true".asExpressionLike()
    val choices = mutableListOf<RandomTaskChoice>()

    override fun createTasks(
        entity: LivingEntity,
        brainConfigurationContext: BrainConfigurationContext
    ): List<BehaviorControl<in LivingEntity>> {
        runtime.withQueryValue("entity", (entity as? PosableEntity)?.struct ?: QueryStruct(hashMapOf()))
        if (!runtime.resolveBoolean(condition)) return emptyList()
        val task = choices.weightedSelection { it.weight }?.task ?: throw IllegalStateException("No tasks to choose from in random_task config")
        return task.createTasks(entity, brainConfigurationContext)
    }
}