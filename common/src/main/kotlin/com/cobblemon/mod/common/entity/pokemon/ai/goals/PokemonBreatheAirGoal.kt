/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.goals

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.closestPosition
import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.entity.ai.goal.BreathAirGoal
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.pathfinder.PathComputationType

class PokemonBreatheAirGoal(val pokemonEntity: PokemonEntity) : BreathAirGoal(pokemonEntity) {
    override fun canUse(): Boolean {
        val canSwimUnderwater = pokemonEntity.behaviour.moving.swim.canBreatheUnderwater
        return if (pokemonEntity.isInLava && pokemonEntity.behaviour.moving.swim.canSwimInLava) {
            true
        } else if (pokemonEntity.isInWater && (pokemonEntity.behaviour.moving.swim.avoidsWater || (!canSwimUnderwater && pokemonEntity.random.nextFloat() < 0.1)) && pokemonEntity.ownerUUID == null) {
            true
        } else {
            super.canUse()
        }
    }

    override fun start() {
        moveToBreathable()
    }

    override fun tick() {
        if (pokemonEntity.navigation.isDone) {
            moveToBreathable()
        }
    }

    fun moveToBreathable() {
        val behaviour = pokemonEntity.behaviour
        if (!behaviour.moving.swim.canBreatheUnderwater) {
            val iterable = BlockPos.betweenClosed(
                Mth.floor(pokemonEntity.x - 8.0),
                pokemonEntity.blockY,
                Mth.floor(pokemonEntity.z - 8.0),
                Mth.floor(pokemonEntity.x + 8.0),
                Mth.floor(pokemonEntity.y + 2.0),
                Mth.floor(pokemonEntity.z + 8.0)
            )
            val blockPos = pokemonEntity.closestPosition(iterable) { isAirPos(pokemonEntity.level(), it) }
                ?: return
            pokemonEntity.navigation.moveTo(blockPos.x.toDouble(), (blockPos.y + 1).toDouble(), blockPos.z.toDouble(), 1.0)
        }
    }

    private fun isAirPos(world: LevelReader, pos: BlockPos): Boolean {
        val blockState = world.getBlockState(pos)
        val aboveState = world.getBlockState(pos.above())
        val notFluid = world.getFluidState(pos.above()).isEmpty || blockState.`is`(Blocks.BUBBLE_COLUMN)
        val canPathfindThroughAbove = aboveState.isPathfindable(PathComputationType.LAND)
        val solidBelow = !blockState.isPathfindable(PathComputationType.LAND)

        return notFluid && canPathfindThroughAbove && solidBelow
    }
}