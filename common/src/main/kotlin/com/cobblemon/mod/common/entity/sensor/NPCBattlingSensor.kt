/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.sensor

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.entity.npc.NPCEntity
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.ai.sensing.Sensor

class NPCBattlingSensor : Sensor<NPCEntity>() {
    companion object {
        val OUTPUT_MEMORY_MODULES = setOf(CobblemonMemories.NPC_BATTLING)
    }

    override fun requires() = OUTPUT_MEMORY_MODULES

    override fun doTick(world: ServerLevel, entity: NPCEntity) {
        val isBattling = entity.isInBattle()
        if (isBattling) {
            entity.getBrain().setMemory(CobblemonMemories.NPC_BATTLING, true)
        } else if (entity.getBrain().hasMemoryValue(CobblemonMemories.NPC_BATTLING)) {
            entity.getBrain().eraseMemory(CobblemonMemories.NPC_BATTLING)
        }
    }
}