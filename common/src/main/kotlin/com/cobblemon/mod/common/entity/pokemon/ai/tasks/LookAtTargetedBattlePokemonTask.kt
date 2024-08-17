/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.tasks

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.world.entity.ai.behavior.EntityTracker
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType

/**
 * Manages the look target of a Pokémon in battle.
 *
 * @author Hiroku
 * @since April 8th, 2024
 */
object LookAtTargetedBattlePokemonTask {
    fun create(): OneShot<PokemonEntity> {
        return BehaviorBuilder.create {
            it.group(
                it.present(CobblemonMemories.POKEMON_BATTLE),
                it.registered(CobblemonMemories.TARGETED_BATTLE_POKEMON),
                it.registered(MemoryModuleType.LOOK_TARGET),
            ).apply(it) { _, targetedBattlePokemon, lookTarget ->
                Trigger { world, entity, _ ->
                    val targeted = it.tryGet(targetedBattlePokemon).orElse(null)
                    val targetedEntity = targeted?.let { world.getEntity(it) as? PokemonEntity }
                    val look = it.tryGet(lookTarget).orElse(null) as? EntityTracker
                    if (targeted != null && targetedEntity == null) {
                        entity.brain.eraseMemory(CobblemonMemories.TARGETED_BATTLE_POKEMON)
                        return@Trigger false
                    } else if (targetedEntity != null && look?.entity != targetedEntity) {
                        entity.brain.setMemory(MemoryModuleType.LOOK_TARGET, EntityTracker(targetedEntity, true))
                        return@Trigger true
                    } else if (targetedEntity == null) {
                        val battle = entity.battle ?: return@Trigger false
                        val nearestOpposingPokemon = battle.sides
                            .find { entity in it.actors.flatMap { it.pokemonList.map { it.entity } } }
                            ?.getOppositeSide()?.actors
                            ?.flatMap { it.pokemonList.mapNotNull { it.entity } }
                            ?.minByOrNull { it.distanceTo(entity) }
                        if (nearestOpposingPokemon != null) {
                            entity.brain.setMemory(MemoryModuleType.LOOK_TARGET, EntityTracker(nearestOpposingPokemon, true))
                            return@Trigger true
                        }
                    }
                    return@Trigger false
                }
            }
        }
    }
}