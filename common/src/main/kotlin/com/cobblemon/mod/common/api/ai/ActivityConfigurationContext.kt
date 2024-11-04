/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai

import com.google.common.collect.ImmutableList
import com.mojang.datafixers.util.Pair
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.schedule.Activity

class ActivityConfigurationContext(val activity: Activity) {
    val tasks = mutableListOf<Pair<Int, BehaviorControl<in LivingEntity>>>()

    fun addTasks(weight: Int, vararg tasks: BehaviorControl<in LivingEntity>) {
        this.tasks.addAll(tasks.map { Pair.of(weight, it) })
    }

    fun apply(entity: LivingEntity) {
        entity.brain.addActivity(activity, ImmutableList.copyOf(tasks))
    }
}