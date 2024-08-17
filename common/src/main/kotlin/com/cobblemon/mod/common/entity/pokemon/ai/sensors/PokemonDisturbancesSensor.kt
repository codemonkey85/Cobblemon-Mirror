/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.ai

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.Sensor

class PokemonDisturbancesSensor : Sensor<PokemonEntity>() {
    override fun doTick(world: ServerLevel, entity: PokemonEntity) {
        val nearestPlayers = world.players()
                .filter { it.isAlive && entity.distanceToSqr(it) <= 16 * 16 }
                .minByOrNull { entity.distanceToSqr(it) }

        nearestPlayers?.let {
            entity.brain.setMemory(MemoryModuleType.DISTURBANCE_LOCATION, BlockPos(it.blockPosition()))
        }
    }

    override fun requires(): Set<MemoryModuleType<*>> {
        return setOf(MemoryModuleType.DISTURBANCE_LOCATION)
    }
}
