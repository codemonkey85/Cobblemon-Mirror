/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl

class WrapperLivingEntityTask<T : LivingEntity>(val task: BehaviorControl<T>, val clazz: Class<T>) : BehaviorControl<LivingEntity> {
    override fun tryStart(level: ServerLevel, entity: LivingEntity, gameTime: Long): Boolean {
        if (clazz.isInstance(entity)) {
            return task.tryStart(level, entity as T, gameTime)
        } else {
            return false
        }
    }

    override fun debugString() = task.debugString()
    override fun tickOrStop(level: ServerLevel, entity: LivingEntity, gameTime: Long) {
        task.tickOrStop(level, entity as T, gameTime)
    }

    override fun getStatus() = task.status
    override fun doStop(level: ServerLevel, entity: LivingEntity, gameTime: Long) = task.doStop(level, entity as T, gameTime)
}