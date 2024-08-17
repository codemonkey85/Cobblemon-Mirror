/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.sensors

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.AgeableMob
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities
import net.minecraft.world.entity.ai.sensing.Sensor

/**
 * Senses when the Pokémon is near an adult of the same species.
 *
 * @author broccoli
 * @since May 15th, 2024
 */
class PokemonAttackablesSensor : Sensor<PokemonEntity>(100) {
    override fun requires() = setOf(MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)

    override fun doTick(world: ServerLevel, entity: PokemonEntity) {
        entity.brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).ifPresent { visibleMobs ->
            setNearestPokemonAdult(entity, visibleMobs)
        }
    }

    private fun setNearestPokemonAdult(entity: PokemonEntity, visibleMobs: NearestVisibleLivingEntities) {
        val nearestAdult = visibleMobs.findClosest { mob ->
            targetIsValidForChild(entity, mob)
        }.map { mob -> mob as AgeableMob }

        entity.brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT, nearestAdult)
    }

    private fun targetIsValidForChild(child: PokemonEntity, entity: LivingEntity): Boolean {
        val potentialParent = entity as? PokemonEntity ?: return false
        val childSpecies = child.pokemon.species.resourceIdentifier

        var preEvolution = potentialParent.pokemon.preEvolution

        while (preEvolution != null) {
            if (childSpecies == preEvolution.species.resourceIdentifier) {
                LOGGER.info("found parent {} for child {}", entity.pokemon.species.name, child.pokemon.species.name)
                return true
            }

            preEvolution = preEvolution.species.preEvolution
        }

        return false
    }
}