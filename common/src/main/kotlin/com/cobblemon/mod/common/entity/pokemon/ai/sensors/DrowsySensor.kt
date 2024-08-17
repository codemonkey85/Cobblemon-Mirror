/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.sensors

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.Sensor

/**
 * Senses when the Pokémon is in a situation that would make it ready to sleep.
 *
 * @author Hiroku
 * @since March 23rd, 2024
 */
class DrowsySensor : Sensor<PokemonEntity>(100) {
    override fun requires() = setOf(CobblemonMemories.POKEMON_DROWSY)
    override fun doTick(world: ServerLevel, entity: PokemonEntity) {
        val rest = entity.behaviour.resting
        val isDrowsy = entity.brain.getMemory(CobblemonMemories.POKEMON_DROWSY).orElse(false)
        val shouldBeDrowsy = rest.canSleep && world.dayTime.toInt() in rest.times && entity.brain.getMemory(MemoryModuleType.ANGRY_AT).isEmpty
        if (!isDrowsy && shouldBeDrowsy) {
            entity.brain.setMemory(CobblemonMemories.POKEMON_DROWSY, true)
        } else if (isDrowsy && !shouldBeDrowsy) {
            entity.brain.eraseMemory(CobblemonMemories.POKEMON_DROWSY)
        }
    }
}