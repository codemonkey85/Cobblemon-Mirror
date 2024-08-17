/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.tasks

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.google.common.collect.ImmutableMap
import kotlin.math.atan2
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.tags.FluidTags
import net.minecraft.util.Mth
import net.minecraft.world.entity.ai.behavior.Behavior
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.minecraft.world.level.material.FluidState

class JumpOutOfWaterTask : Behavior<PokemonEntity>(
    ImmutableMap.of(
        MemoryModuleType.IS_IN_WATER, MemoryStatus.VALUE_PRESENT,
        MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
    )
){
    var inWater = false
    companion object {
        private const val MAX_DURATION = 80
        private const val CHANCE = 10
        private val OFFSET_MULTIPLIERS = intArrayOf(0, 1, 4, 5, 6, 7)
    }

    override fun checkExtraStartConditions(world: ServerLevel, entity: PokemonEntity): Boolean {
        if(entity.random.nextInt(CHANCE) != 0) {
            return false
        } else {
            val direction = entity.motionDirection
            val x = direction.stepX
            val z = direction.stepZ
            val blockPos = entity.blockPosition()
            val offsets = OFFSET_MULTIPLIERS
            for (offset in offsets) {
                if (!this.isWater(entity, blockPos, x, z, offset) || !this.isAirAbove(entity, blockPos, x, z, offset)) {
                    return false
                }
            }
            return true
        }
    }

    override fun canStillUse(world: ServerLevel, entity: PokemonEntity, time: Long): Boolean {
        val d: Double = entity.deltaMovement.y
        return (!(d * d < 0.029999999329447746) || entity.xRot == 0.0f || !(Math.abs(entity.xRot) < 10.0f) || !entity.isInWater) && !entity.onGround()
    }

    override fun start(world: ServerLevel, entity: PokemonEntity, time: Long) {
        val direction = entity.motionDirection
        val newVelocity = entity.deltaMovement.add(direction.stepX.toDouble() * 0.6, 0.7, direction.stepZ.toDouble() * 0.6)
        entity.deltaMovement = newVelocity
        entity.getNavigation().stop()
    }

    override fun tick(world: ServerLevel, entity: PokemonEntity, time: Long) {
        val bl: Boolean = this.inWater
        if (!bl) {
            val fluidState: FluidState = entity.level().getFluidState(entity.blockPosition())
            this.inWater = fluidState.`is`(FluidTags.WATER)
        }

        if (this.inWater && !bl) {
            entity.playSound(SoundEvents.DOLPHIN_JUMP, 1.0f, 1.0f)
        }

        val vec3d = entity.deltaMovement
        if (vec3d.y * vec3d.y < 0.029999999329447746 && entity.xRot != 0.0f) {
            entity.xRot = Mth.rotLerp(0.2f, entity.xRot, 0.0f)
        } else if (vec3d.length() > 9.999999747378752E-6) {
            val d = vec3d.horizontalDistance()
            val e = atan2(-vec3d.y, d) * 57.2957763671875
            entity.xRot = e.toFloat()
        }
    }

    
    
    private fun isWater(entity: PokemonEntity, pos: BlockPos, offsetX: Int, offsetZ: Int, multiplier: Int): Boolean {
        val blockPos = pos.offset(offsetX * multiplier, 0, offsetZ * multiplier)
        return entity.level().getFluidState(blockPos).`is`(FluidTags.WATER) && !entity.level().getBlockState(blockPos).blocksMotion()
    }

    private fun isAirAbove(entity: PokemonEntity, pos: BlockPos, offsetX: Int, offsetZ: Int, multiplier: Int): Boolean {
        return entity.level().getBlockState(pos.offset(offsetX * multiplier, 1, offsetZ * multiplier))
            .isAir && entity.level().getBlockState(pos.offset(offsetX * multiplier, 2, offsetZ * multiplier))
            .isAir
    }
}