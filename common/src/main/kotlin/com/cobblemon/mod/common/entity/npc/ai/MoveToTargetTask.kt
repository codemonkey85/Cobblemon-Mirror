/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.npc.ai

import com.cobblemon.mod.common.entity.npc.NPCEntity
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType

object MoveToTargetTask {
    fun create(): OneShot<NPCEntity> {
        return BehaviorBuilder.create {
            it.group(
                it.absent(MemoryModuleType.PATH),
                it.present(MemoryModuleType.WALK_TARGET)
            ).apply(it) { path, walkTarget ->
                Trigger { world, entity, time ->
                    val targetVec = it.get(walkTarget).target.currentBlockPosition()
                    val walkPath = entity.navigation.createPath(targetVec, 0)
                    if (walkPath == null) {
                        walkTarget.erase()
                    } else {
                        path.set(walkPath)
                    }
                    return@Trigger true
                }
            }
        }
    }
}