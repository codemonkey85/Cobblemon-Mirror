package com.cobblemon.mod.common.api.ai.config.task

import com.cobblemon.mod.common.api.ai.BrainConfigurationContext
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl

fun interface TaskConfig {
    companion object {
        val types = mutableMapOf<String, Class<out TaskConfig>>(
            "run_one" to RunOneTaskConfig::class.java
        )
    }

    fun createTask(entity: LivingEntity, brainConfigurationContext: BrainConfigurationContext): BehaviorControl<in LivingEntity>
}