/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.ai

import com.cobblemon.mod.common.CobblemonMemories
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink

class LookAroundTaskWrapper(minRunTime: Int, maxRunTime: Int) : LookAtTargetSink(minRunTime, maxRunTime) {
    override fun checkExtraStartConditions(world: ServerLevel, entity: Mob): Boolean {
        return (!entity.brain.hasMemoryValue(CobblemonMemories.POKEMON_SLEEPING) || entity.brain.isMemoryValue(CobblemonMemories.POKEMON_SLEEPING, false)) && super.checkExtraStartConditions(world, entity)
    }
}