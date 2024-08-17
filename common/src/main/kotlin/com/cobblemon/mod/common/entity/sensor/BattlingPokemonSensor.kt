/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.sensor

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.Sensor

/**
 * Sensor used to detect all of the Pokémon entities that are participating in a battle that this entity is a part of.
 *
 * @author Hiroku
 * @since January 6th, 2024
 */
class BattlingPokemonSensor : Sensor<LivingEntity>(20) {
    companion object {
        val OUTPUT_MEMORY_MODULES = setOf<MemoryModuleType<*>>(CobblemonMemories.BATTLING_POKEMON)
    }

    override fun requires() = OUTPUT_MEMORY_MODULES

    override fun doTick(world: ServerLevel, entity: LivingEntity) {
        val battles = when (entity) {
            is PokemonEntity -> entity.battleId?.let { BattleRegistry.getBattle(it) }?.let { listOf(it) } ?: emptyList()
            is NPCEntity -> entity.battleIds.mapNotNull(BattleRegistry::getBattle)
            else -> emptyList()
        }

        val entityUUIDs = battles.flatMap { it.activePokemon }.mapNotNull { it.battlePokemon?.entity?.uuid }.filterNot { it == entity.uuid }
        entity.brain.setMemory(CobblemonMemories.BATTLING_POKEMON, entityUUIDs)
    }

}