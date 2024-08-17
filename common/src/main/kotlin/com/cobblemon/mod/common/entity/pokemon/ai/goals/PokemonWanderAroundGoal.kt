/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.goals

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.canFit
import com.cobblemon.mod.common.util.toVec3d
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.tags.TagKey
import net.minecraft.core.BlockPos
import net.minecraft.tags.BlockTags
import net.minecraft.tags.FluidTags
import net.minecraft.world.entity.ai.goal.RandomStrollGoal
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.phys.Vec3

/**
 * An override of the [WanderAroundGoal] so that Pokémon behaviours can be implemented.
 *
 * @author Hiroku
 * @since July 30th, 2022
 */
class PokemonWanderAroundGoal(val entity: PokemonEntity) : RandomStrollGoal(entity, entity.behaviour.moving.wanderSpeed) {
    init {
        interval = entity.behaviour.moving.wanderChance
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun canMove(): Boolean {
        val moving = entity.behaviour.moving
        return moving.walk.canWalk || moving.fly.canFly || (moving.swim.canBreatheUnderwater && mob.isEyeInFluid(FluidTags.WATER))
    }

    override fun canUse() = super.canUse() && canMove() && !entity.isBusy && (entity.ownerUUID == null || entity.tethering != null)

    override fun canContinueToUse() = super.canContinueToUse() && canMove() && !entity.isBusy

    override fun getPosition(): Vec3? {
        val moving = entity.behaviour.moving
        if (entity.isEyeInFluid(FluidTags.WATER) && moving.swim.canBreatheUnderwater) {
            getFluidTarget(FluidTags.WATER)?.let { return it }
        } else if (entity.isEyeInFluid(FluidTags.LAVA) && moving.swim.canBreatheUnderlava) {
            return getFluidTarget(FluidTags.LAVA)
        } else if (entity.isInWater && moving.swim.canWalkOnWater) {
            getFluidTarget(FluidTags.WATER)?.let { return it }
        } else if (entity.isInLava && moving.swim.canWalkOnLava) {
            getFluidTarget(FluidTags.LAVA)?.let { return it }
        }

        if (moving.walk.avoidsLand) {
            return null
        }

        return getLandTarget()
    }

    @Suppress("DEPRECATION", "MemberVisibilityCanBePrivate")
    fun getLandTarget(): Vec3? {
        val roamDistanceCondition: (BlockPos) -> Boolean = { entity.tethering?.canRoamTo(it) != false }
        val iterable: Iterable<BlockPos> = BlockPos.randomBetweenClosed(entity.random, 64, entity.blockX - 10, entity.blockY, entity.blockZ - 10, entity.blockX + 10, entity.blockY, entity.blockZ + 10)
        val condition: (BlockState, BlockPos) -> Boolean = { _, pos -> entity.canFit(pos) && roamDistanceCondition(pos) }
        val iterator = iterable.iterator()
        position@
        while (iterator.hasNext()) {
            val pos = iterator.next().mutable()
            var blockState = entity.level().getBlockState(pos)

            val maxSteps = 16
            var steps = 0
            var good = false
            if (!blockState.isSolid && !blockState.liquid()) {
                pos.move(0, -1, 0)
                var previousWasAir = true
                while (steps++ < maxSteps && pos.y > entity.level().minBuildHeight) {
                    if (pos.y <= entity.level().minBuildHeight) {
                        continue@position
                    }
                    blockState = entity.level().getBlockState(pos)
                    if (blockState.isSolid && !blockState.`is`(BlockTags.LEAVES) && previousWasAir) {
                        pos.move(0, 1, 0)
                        blockState = entity.level().getBlockState(pos)
                        good = true
                        break
                    } else {
                        previousWasAir = blockState.isAir
                    }
                    pos.move(0, -1, 0)
                }
            } else {
                var previousWasSolid = blockState.isSolid && !blockState.`is`(BlockTags.LEAVES)
                pos.move(0, 1, 0)
                while (steps++ < maxSteps) {
                    if (pos.y >= entity.level().maxBuildHeight) {
                        continue@position
                    }
                    blockState = entity.level().getBlockState(pos)
                    if (blockState.isAir && previousWasSolid) {
                        good = true
                        break
                    }
                    previousWasSolid = blockState.isSolid && !blockState.`is`(BlockTags.LEAVES)
                    pos.move(0, 1, 0)
                }
            }

            if (good && condition(blockState, pos)) {
                return pos.toVec3d()
            }
        }

        return null
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun getFluidTarget(fluidTag: TagKey<Fluid>): Vec3? {
        val roamDistanceCondition: (BlockPos) -> Boolean = { entity.tethering?.canRoamTo(it) != false }
        val walksOnFloor = !entity.behaviour.moving.swim.canSwimInFluid(fluidTag)
        var iterable: Iterable<BlockPos> = BlockPos.randomInCube(entity.random, 32, entity.blockPosition(), 12)
        var condition: (BlockState, BlockPos) -> Boolean = { blockState, pos -> roamDistanceCondition(pos) && blockState.fluidState.`is`(fluidTag) && entity.canFit(pos) }
        if (walksOnFloor) {
            condition = { blockState, blockPos ->
                val down = blockPos.below()
                val below = entity.level().getBlockState(down)
                roamDistanceCondition(blockPos) && blockState.fluidState.`is`(fluidTag) && below.isRedstoneConductor(entity.level(), down) && entity.canFit(blockPos)
            }
        }
        if (entity.level().isEmptyBlock(entity.blockPosition().above())) {
            if ((fluidTag == FluidTags.WATER && entity.behaviour.moving.swim.canWalkOnWater) ||
                    fluidTag == FluidTags.LAVA && entity.behaviour.moving.swim.canWalkOnLava) {
                iterable = BlockPos.randomBetweenClosed(entity.random, 16, entity.blockX - 16, entity.blockY, entity.blockZ - 16, entity.blockX + 16, entity.blockY, entity.blockZ + 16)
            }
        }
        val iterator = iterable.iterator()
        while (iterator.hasNext()) {
            val pos = iterator.next()
            val blockState = entity.level().getBlockState(pos)
            if (condition(blockState, pos)) {
                return pos.toVec3d()
            }
        }

        return null
    }
}