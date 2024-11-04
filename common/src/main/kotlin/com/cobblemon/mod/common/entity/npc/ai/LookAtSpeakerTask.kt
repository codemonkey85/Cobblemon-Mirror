/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.npc.ai

import com.cobblemon.mod.common.CobblemonMemories
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.EntityTracker
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType

object LookAtSpeakerTask {
    fun create(closeEnough: Float = 0.5F): OneShot<LivingEntity> {
        return BehaviorBuilder.create {
            it.group(
                it.registered(MemoryModuleType.LOOK_TARGET),
                it.present(CobblemonMemories.DIALOGUES)
            ).apply(it) { lookTarget, dialogues ->
                Trigger { _, _, _ ->
                    val possibleLookTargets = it.get(dialogues).map { it.playerEntity }
                    val currentLookTarget = it.tryGet(lookTarget).orElse(null)
                    if (currentLookTarget != null && possibleLookTargets.any { it.eyePosition.distanceTo(currentLookTarget.currentPosition()) < closeEnough }) {
                        return@Trigger false
                    } else if (currentLookTarget != null) {
                        lookTarget.erase()
                    }

                    val lookEntity = possibleLookTargets.randomOrNull() ?: return@Trigger false
                    lookTarget.set(EntityTracker(lookEntity, true))
                    return@Trigger true
                }
            }
        }
    }
}