/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config

import com.cobblemon.mod.common.api.ai.BrainConfigurationContext
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.schedule.Activity

class SetDefaultActivity : BrainConfig {
    val activity: Activity = Activity.IDLE
    override fun configure(entity: LivingEntity, brainConfigurationContext: BrainConfigurationContext) {
        brainConfigurationContext.defaultActivity = activity
    }
}