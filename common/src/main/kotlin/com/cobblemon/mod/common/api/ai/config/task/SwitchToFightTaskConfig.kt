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
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.resolveBoolean
import com.cobblemon.mod.common.util.withQueryValue
import net.minecraft.world.Difficulty
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.schedule.Activity

class SwitchToFightTaskConfig : SingleTaskConfig {
    val condition = "true".asExpressionLike()
    val activity = Activity.FIGHT

    override fun createTask(
        entity: LivingEntity,
        brainConfigurationContext: BrainConfigurationContext
    ) = BehaviorBuilder.create {
        it.group(
            it.present(MemoryModuleType.ATTACK_TARGET)
        ).apply(it) { _ ->
            Trigger { world, entity, _ ->
                runtime.withQueryValue("entity", (entity as? PosableEntity)?.struct ?: QueryStruct(hashMapOf()))
                if (!runtime.resolveBoolean(condition)) return@Trigger false
                if (entity.commandSenderWorld.getCurrentDifficultyAt(entity.blockPosition()).difficulty == Difficulty.PEACEFUL) return@Trigger false

                entity.brain.setActiveActivityIfPossible(activity)
                return@Trigger true
            }
        }
    }
}