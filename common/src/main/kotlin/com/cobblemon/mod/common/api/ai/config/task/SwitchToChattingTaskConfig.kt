/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.bedrockk.molang.runtime.struct.QueryStruct
import com.cobblemon.mod.common.CobblemonActivities
import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.api.ai.BrainConfigurationContext
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.resolveBoolean
import com.cobblemon.mod.common.util.withQueryValue
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType

class SwitchToChattingTaskConfig : SingleTaskConfig {
    val condition = "true".asExpressionLike()
    override fun createTask(
        entity: LivingEntity,
        brainConfigurationContext: BrainConfigurationContext
    ) = BehaviorBuilder.create {
        it.group(
            it.present(CobblemonMemories.DIALOGUES),
            it.registered(MemoryModuleType.WALK_TARGET)
        ).apply(it) { _, walkTarget ->
            Trigger { world, entity, _ ->
                runtime.withQueryValue("entity", (entity as? PosableEntity)?.struct ?: QueryStruct(hashMapOf()))
                if (!runtime.resolveBoolean(condition)) return@Trigger false
                walkTarget.erase()
                entity.brain.setActiveActivityIfPossible(CobblemonActivities.NPC_CHATTING)
                return@Trigger true
            }
        }
    }
}