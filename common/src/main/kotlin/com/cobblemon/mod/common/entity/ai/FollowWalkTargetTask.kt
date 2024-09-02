/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.ai

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.behavior.Behavior
import net.minecraft.world.entity.ai.behavior.EntityTracker
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.minecraft.world.entity.ai.memory.WalkTarget
import net.minecraft.world.entity.ai.util.DefaultRandomPos
import net.minecraft.world.level.pathfinder.Path
import net.minecraft.world.phys.Vec3

class FollowWalkTargetTask(
    minRunTime: Int = 150,
    maxRunTime: Int = 250
) : Behavior<PathfinderMob>(
    mapOf(
        MemoryModuleType.WALK_TARGET to MemoryStatus.VALUE_PRESENT,
        MemoryModuleType.PATH to MemoryStatus.VALUE_ABSENT,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE to MemoryStatus.REGISTERED
    ), minRunTime, maxRunTime
) {
    private var pathUpdateCountdownTicks = 0
    private var path: Path? = null
    private var lookTargetPos: BlockPos? = null
    private var speed = 0f

    override fun checkExtraStartConditions(arg: ServerLevel, arg2: PathfinderMob): Boolean {
        if (this.pathUpdateCountdownTicks > 0) {
            --this.pathUpdateCountdownTicks
            return false
        } else {
            val brain = arg2.brain
            val walkTarget = brain.getMemory(MemoryModuleType.WALK_TARGET).get()
            val hasReached = this.hasReached(arg2, walkTarget)
            if (!hasReached && this.hasFinishedPath(arg2, walkTarget, arg.gameTime)) {
                this.lookTargetPos = walkTarget.target.currentBlockPosition()
                return true
            } else {
                brain.eraseMemory(MemoryModuleType.WALK_TARGET)
                if (hasReached) {
                    brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
                }

                return false
            }
        }
    }

    override fun canStillUse(arg: ServerLevel, arg2: PathfinderMob, l: Long): Boolean {
        if (this.path != null && this.lookTargetPos != null) {
            val optional = arg2.brain.getMemory(MemoryModuleType.WALK_TARGET)
            val bl = optional.map(::isTargetSpectator).orElse(false)
            val entityNavigation = arg2.navigation
            return !entityNavigation.isDone && optional.isPresent && !this.hasReached(arg2, optional.get()) && !bl
        } else {
            return false
        }
    }

    override fun stop(world: ServerLevel, entity: PathfinderMob, l: Long) {
        val walkTarget = entity.brain.getMemory(MemoryModuleType.WALK_TARGET).orElse(null)
        if (walkTarget != null && !this.hasReached(entity, walkTarget) && entity.navigation.isStuck) {
            this.pathUpdateCountdownTicks = world.getRandom().nextInt(40)
        }

        entity.navigation.stop()
        entity.brain.eraseMemory(MemoryModuleType.WALK_TARGET)
        entity.brain.eraseMemory(MemoryModuleType.PATH)
        this.path = null
    }

    override fun start(arg: ServerLevel, arg2: PathfinderMob, l: Long) {
        arg2.brain.setMemory(MemoryModuleType.PATH, this.path)
        arg2.navigation.moveTo(this.path, speed.toDouble())
    }

    override fun tick(world: ServerLevel, entity: PathfinderMob, l: Long) {
        val path = entity.navigation.path
        val brain = entity.brain
        if (this.path !== path) {
            this.path = path
            brain.setMemory(MemoryModuleType.PATH, path)
        }

        if (path != null && lookTargetPos != null) {
            val walkTarget = brain.getMemory(MemoryModuleType.WALK_TARGET).get()
            if (walkTarget.target.currentBlockPosition().distSqr(lookTargetPos) > 4.0 && hasFinishedPath(entity, walkTarget, world.gameTime)) {
                lookTargetPos = walkTarget.target.currentBlockPosition()
                start(world, entity, l)
            }
        }
    }

    private fun hasFinishedPath(entity: PathfinderMob, walkTarget: WalkTarget, time: Long): Boolean {
        val blockPos = walkTarget.target.currentBlockPosition()
        this.path = entity.navigation.createPath(blockPos, 0)
        this.speed = walkTarget.speedModifier
        val brain = entity.brain
        if (hasReached(entity, walkTarget)) {
            brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
        } else {
            val bl = this.path != null && path!!.canReach()
            if (bl) {
                brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
            } else if (!brain.hasMemoryValue(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)) {
                brain.setMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, time)
            }

            if (this.path != null) {
                return true
            }

            val vec3d = DefaultRandomPos.getPosTowards(
                entity,
                10,
                7,
                Vec3.atBottomCenterOf(blockPos),
                1.5707963705062866
            )
            if (vec3d != null) {
                this.path = entity.navigation.createPath(vec3d.x, vec3d.y, vec3d.z, 0)
                return this.path != null
            }
        }

        return false
    }

    private fun hasReached(entity: PathfinderMob, walkTarget: WalkTarget): Boolean {
        return walkTarget.target.currentBlockPosition().distManhattan(entity.blockPosition()) <= walkTarget.closeEnoughDist
    }

    companion object {
        private const val MAX_UPDATE_COUNTDOWN = 40
        private fun isTargetSpectator(target: WalkTarget): Boolean {
            val lookTarget = target.target
            return if (lookTarget is EntityTracker) {
                lookTarget.entity.isSpectator
            } else {
                false
            }
        }
    }
}
