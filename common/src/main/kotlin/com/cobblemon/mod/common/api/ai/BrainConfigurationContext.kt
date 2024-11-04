/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai

import com.cobblemon.mod.common.api.ai.config.BrainConfig
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.schedule.Activity
import net.minecraft.world.entity.schedule.Schedule

class BrainConfigurationContext {

    var defaultActivity = Activity.IDLE
    var coreActivities = setOf(Activity.CORE)
    val activities = mutableListOf<ActivityConfigurationContext>()
    val schedule = Schedule.EMPTY

    fun getOrCreateActivity(activity: Activity): ActivityConfigurationContext {
        return activities.firstOrNull { it.activity == activity } ?: ActivityConfigurationContext(activity).also(activities::add)
    }

    fun apply(entity: LivingEntity, brainConfigs: List<BrainConfig>) {
        val brain = entity.brain

        // Setup the brain config
        brainConfigs.forEach { it.configure(entity, this) }

        // Apply the brain config
        activities.forEach { it.apply(entity) }
        brain.setCoreActivities(coreActivities)
        brain.setDefaultActivity(defaultActivity)
        brain.schedule = schedule
        brain.setActiveActivityIfPossible(defaultActivity)
    }
}