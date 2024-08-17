/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.ai

import com.cobblemon.mod.common.CobblemonMemories
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder

object LookAtMobTaskWrapper {
    fun create(range:Float): OneShot<LivingEntity> {
        return BehaviorBuilder.create {
            it.group(
                it.absent(CobblemonMemories.POKEMON_SLEEPING),
            ).apply(it) { isSleeping ->
                return@apply SetEntityLookTarget.create(range)
            }
        }
    }
}