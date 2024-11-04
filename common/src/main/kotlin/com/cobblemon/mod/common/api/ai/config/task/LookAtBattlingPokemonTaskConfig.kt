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
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.entity.npc.ai.LookAtBattlingPokemonTask
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.resolveInt
import com.cobblemon.mod.common.util.withQueryValue
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl

class LookAtBattlingPokemonTaskConfig : SingleTaskConfig {
    val minDurationTicks = "40".asExpression()
    val maxDurationTicks = "80".asExpression()

    override fun createTask(
        entity: LivingEntity,
        brainConfigurationContext: BrainConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        runtime.withQueryValue("entity", (entity as? PosableEntity)?.struct ?: QueryStruct(hashMapOf()))
        return LookAtBattlingPokemonTask.create(
            minDurationTicks = runtime.resolveInt(minDurationTicks),
            maxDurationTicks = runtime.resolveInt(maxDurationTicks)
        )
    }

}