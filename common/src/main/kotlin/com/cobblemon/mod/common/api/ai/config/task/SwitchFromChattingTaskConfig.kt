/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.api.ai.BrainConfigurationContext
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.schedule.Activity

class SwitchFromChattingTaskConfig : SingleTaskConfig {
    val activity = Activity.IDLE
    override fun createTask(
        entity: LivingEntity,
        brainConfigurationContext: BrainConfigurationContext
    ) = BehaviorBuilder.create {
        it.group(
            it.absent(CobblemonMemories.DIALOGUES)
        ).apply(it) { _ ->
            Trigger { world, entity, _ ->
                entity.brain.setActiveActivityIfPossible(activity)
                return@Trigger true
            }
        }
    }
}