package com.cobblemon.mod.common.api.ai.config

import com.cobblemon.mod.common.api.ai.BrainConfigurationContext
import net.minecraft.world.entity.LivingEntity

object DummyBrainConfig : BrainConfig {
    override fun configure(entity: LivingEntity, brainConfigurationContext: BrainConfigurationContext) {}
}