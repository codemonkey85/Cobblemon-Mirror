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
import net.minecraft.tags.FluidTags
import net.minecraft.tags.TagKey
import net.minecraft.util.Mth
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.math.ceil
import kotlin.math.floor

class PokemonMoveIntoFluidGoal(private val mob: PokemonEntity) : Goal() {
    override fun canUse(): Boolean {
        val avoidsLand = mob.behaviour.moving.walk.avoidsLand
        if (!avoidsLand || mob.owner != null) {
            return false
        }

        val allowsWater = mob.behaviour.moving.swim.canSwimInWater
        val allowsLava = mob.behaviour.moving.swim.canSwimInLava

        val pos = mob.blockPosition()

        val fluid = mob.level().getFluidState(pos)

        val allowedFluids = mutableListOf<TagKey<Fluid>>()
        if (allowsWater) {
            allowedFluids.add(FluidTags.WATER)
        }
        if (allowsLava) {
            allowedFluids.add(FluidTags.LAVA)
        }

        val allX = floor(mob.boundingBox.minX).toInt()..ceil(mob.boundingBox.maxX).toInt()
        val allZ = floor(mob.boundingBox.minZ).toInt()..ceil(mob.boundingBox.maxZ).toInt()

        var onSolid = false
        val mutablePos = BlockPos.MutableBlockPos()

        outer@
        for (blockX in allX) {
            for (blockZ in allZ) {
                if (mob.level().getBlockState(mutablePos.set(blockX, mob.blockY - 1, blockZ)).isSolid) {
                    onSolid = true
                }
                for (y in mob.blockY..ceil(mob.boundingBox.maxY).toInt()) {
                    val fluidState = mob.level().getFluidState(mutablePos.set(blockX, y, blockZ))
                    if (allowedFluids.any { fluidState.`is`(it) }) {
                        return false
                    } else if (mob.y > 62.0) {
                    }
                }
            }
        }

        return onSolid// && allowedFluids.none { fluid.isIn(it) } && allowedFluids.isNotEmpty()
    }

    override fun start() {
        val appropriateFluids = mutableListOf<TagKey<Fluid>>()
        val swim = mob.behaviour.moving.swim
        if (swim.canSwimInLava && !swim.hurtByLava) {
            appropriateFluids.add(FluidTags.LAVA)
        }
        if (swim.canSwimInWater) {
            appropriateFluids.add(FluidTags.WATER)
        }
        val iterable = BlockPos.betweenClosed(
            Mth.floor(mob.x - 8.0),
            Mth.floor(mob.y - 8.0),
            Mth.floor(mob.z - 8.0),
            Mth.floor(mob.x + 8.0),
            mob.blockY,
            Mth.floor(mob.z + 2.0)
        )

        val box = mob.boundingBox
        val blockPos = mob.closestPosition(iterable) { pos ->
            if (mob.tethering?.canRoamTo(pos) == false) {
                return@closestPosition false
            }
            val fluid = mob.level().getFluidState(pos)
            return@closestPosition appropriateFluids.any { fluid.`is`(it) } &&
                    mob.level().noCollision(
                        AABB.ofSize(
                            Vec3(
                                pos.x.toDouble(),
                                pos.y - 1.0,
                                pos.z.toDouble()
                            ),
                            box.xsize,
                            box.ysize,
                            box.zsize
                        )
                    )
        }
        if (blockPos != null) {
            mob.navigation.moveTo(blockPos.x.toDouble(), blockPos.y.toDouble(), blockPos.z.toDouble(), 1.0)
        }
    }
}