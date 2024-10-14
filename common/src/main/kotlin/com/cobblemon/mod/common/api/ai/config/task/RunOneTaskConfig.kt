package com.cobblemon.mod.common.api.ai.config.task

import com.cobblemon.mod.common.util.toDF
import net.minecraft.world.entity.ai.behavior.DoNothing
import net.minecraft.world.entity.ai.behavior.RunOne

class RunOneTaskConfig : TaskConfig {
    class RunOneTaskOption {
        val weight: Int = 1
        val task: TaskConfig = TaskConfig { _, _ -> DoNothing(0, 1) }
    }

    val options = mutableListOf<RunOneTaskOption>()

    override fun createTask() = RunOne(options.map { it.task.createTask() toDF it.weight })
}