/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config

import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.struct.QueryStruct
import com.cobblemon.mod.common.api.ai.BrainConfigurationContext
import com.cobblemon.mod.common.api.ai.config.task.TaskConfig
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.resolveBoolean
import com.cobblemon.mod.common.util.withQueryValue
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.schedule.Activity

class AddTasksToActivity : BrainConfig {
    val activity = Activity.IDLE
    val condition = "true".asExpressionLike()
    val tasksByPriority = mutableMapOf<Int, List<TaskConfig>>()

    override fun configure(entity: LivingEntity, brainConfigurationContext: BrainConfigurationContext) {
        val runtime = MoLangRuntime().setup()
        runtime.withQueryValue("entity", (entity as? PosableEntity)?.struct ?: QueryStruct(hashMapOf()))
        if (!runtime.resolveBoolean(condition)) return

        val activity = brainConfigurationContext.getOrCreateActivity(activity)
        tasksByPriority.forEach { (priority, taskConfigs) ->
            val tasks = taskConfigs.flatMap { it.createTasks(entity, brainConfigurationContext) }
            activity.addTasks(priority, *tasks.toTypedArray())
        }
    }
}