/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.tasks

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeature
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.DataKeys
import com.google.common.collect.ImmutableMap
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.ai.behavior.Behavior
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate

/**
 * The baa's eat the graa's
 *
 * @author Hiroku
 * @since April 6th, 2024
 */
class EatGrassTask : Behavior<PokemonEntity>(
    ImmutableMap.of(
        MemoryModuleType.ANGRY_AT, MemoryStatus.VALUE_ABSENT,
        MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT,
        MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
        CobblemonMemories.POKEMON_BATTLE, MemoryStatus.VALUE_ABSENT
    )
) {
    val grassPredicate = BlockStatePredicate.forBlock(Blocks.GRASS_BLOCK)
    var timer = -1

    override fun checkExtraStartConditions(world: ServerLevel, entity: PokemonEntity): Boolean {
        if (entity.random.nextInt(500) != 0) {
            return false
        } else {
            entity.pokemon.getFeature<FlagSpeciesFeature>(DataKeys.HAS_BEEN_SHEARED)?.enabled?.takeIf { it } ?: run {
                return false
            }
            val blockPos = entity.blockPosition()
            if (grassPredicate.test(world.getBlockState(blockPos)) ) {
                return true
            } else if (world.getBlockState(blockPos.below()).`is`(Blocks.GRASS_BLOCK)) {
                return true
            }
            return false
        }
    }

    override fun start(world: ServerLevel, entity: PokemonEntity, time: Long) {
        timer = 40
        world.broadcastEntityEvent(entity, 10.toByte())
    }

    override fun canStillUse(world: ServerLevel, entity: PokemonEntity, time: Long): Boolean {
        timer--
        if (timer < 0) {
            val blockPos = entity.blockPosition()
            if (grassPredicate.test(world.getBlockState(blockPos)) ) {
                if (world.gameRules.getBoolean(GameRules.RULE_MOBGRIEFING)) {
                    world.destroyBlock(blockPos, false)
                }
                entity.ate()
            } else if (world.getBlockState(blockPos.below()).`is`(Blocks.GRASS_BLOCK)) {
                val blockPos2 = blockPos.below()
                if (world.getBlockState(blockPos2).`is`(Blocks.GRASS_BLOCK)) {
                    if (world.gameRules.getBoolean(GameRules.RULE_MOBGRIEFING)) {
                        world.globalLevelEvent(2001, blockPos2, Block.getId(Blocks.GRASS_BLOCK.defaultBlockState()))
                        world.setBlock(blockPos2, Blocks.DIRT.defaultBlockState(), 2)
                    }

                    entity.ate()
                }
            }
            return false
        }
        return true
    }
}