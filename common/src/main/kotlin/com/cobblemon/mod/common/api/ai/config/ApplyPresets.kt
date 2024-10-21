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
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonBrainConfigs
import com.cobblemon.mod.common.api.ai.BrainConfigurationContext
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.resolveBoolean
import com.cobblemon.mod.common.util.withQueryValue
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity

class ApplyPresets : BrainConfig {
    val condition = "true".asExpressionLike()
    val presets = mutableListOf<ResourceLocation>()
    override fun configure(entity: LivingEntity, brainConfigurationContext: BrainConfigurationContext) {
        val runtime = MoLangRuntime().setup()
        runtime.withQueryValue("entity", (entity as? PosableEntity)?.struct ?: QueryStruct(hashMapOf()))
        if (!runtime.resolveBoolean(condition)) return

        val presetConfigs = presets.flatMap { CobblemonBrainConfigs.presets[it] ?: return Cobblemon.LOGGER.warn("Preset $it not found") }
        presetConfigs.forEach { it.configure(entity, brainConfigurationContext) }
    }
}