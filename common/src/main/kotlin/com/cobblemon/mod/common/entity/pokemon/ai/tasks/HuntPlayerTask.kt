/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.tasks

import com.google.common.collect.ImmutableMap
import java.util.Optional
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.Behavior
import net.minecraft.world.entity.ai.behavior.EntityTracker
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.minecraft.world.entity.ai.memory.WalkTarget

class HuntPlayerTask : Behavior<LivingEntity>(
        ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.DISTURBANCE_LOCATION, MemoryStatus.VALUE_PRESENT
        )
) {

    companion object {
        private const val MAX_SEARCH_DURATION = 1000
        private const val SEARCH_RADIUS = 20
        private const val ATTACK_RANGE = 2.0
        private const val WALK_SPEED = 0.5f // Adjust this value to change the speed
    }

    private var targetEntity: Optional<LivingEntity> = Optional.empty()
    private var lastKnownLocation: Optional<BlockPos> = Optional.empty()
    private var searching = false
    private var searchTime = 0

    override fun checkExtraStartConditions(world: ServerLevel, entity: LivingEntity): Boolean {
        this.targetEntity = findPlayer(world, entity)
        return targetEntity.isPresent
    }

    override fun canStillUse(world: ServerLevel, entity: LivingEntity, time: Long): Boolean {
        val canSeePlayer = targetEntity.isPresent && entity.hasLineOfSight(targetEntity.get())
        // if entity cannot see player then searching is true
        if (!canSeePlayer)
            searching = true

        val isStillSearching = searching && searchTime < MAX_SEARCH_DURATION

        //return canSeePlayer && isStillSearching   // commented this out for testing
        if (canSeePlayer || isStillSearching)
            return true
        else
            return false
    }

    private fun findPlayer(world: ServerLevel, entity: LivingEntity): Optional<LivingEntity> {
        return world.players()
                // filter all players that are alive that the entity can see and are within a certain radius of the entity
                .filter { it.isAlive && entity.hasLineOfSight(it) && entity.distanceToSqr(it) <= SEARCH_RADIUS * SEARCH_RADIUS }
                // find the player with the minimum distance to the entity
                .minByOrNull { entity.distanceToSqr(it) }
                ?.let { Optional.of(it) } ?: Optional.empty()
    }

    override fun start(world: ServerLevel, entity: LivingEntity, time: Long) {
        addLookWalkTargets(entity)
        searching = false
        searchTime = 0

        super.start(world, entity, time)
    }

    private fun addLookWalkTargets(entity: LivingEntity) {
        targetEntity.ifPresent { target ->
            val lookTarget = EntityTracker(target, true)
            entity.brain.setMemory(MemoryModuleType.LOOK_TARGET, lookTarget)
            entity.brain.setMemory(MemoryModuleType.WALK_TARGET, WalkTarget(lookTarget, WALK_SPEED, 1))
        }
    }

    override fun stop(world: ServerLevel, entity: LivingEntity, time: Long) {
        targetEntity = Optional.empty()
        searching = false
    }

    override fun tick(world: ServerLevel, entity: LivingEntity, time: Long) {
        // if in the searching state
        if (searching) {
            // increase the searchTime counter
            searchTime++

            // if searchTime is above max search time then end run
            if (searchTime >= MAX_SEARCH_DURATION) {
                stop(world, entity, time)
                return
            }
        }

        // if the target is present and entity can see the target player
        if (targetEntity.isPresent && entity.hasLineOfSight(targetEntity.get())) {
            // get target
            val target = targetEntity.get()

            // if target is alive
            if (target.isAlive) {
                // if entity is in range of target
                if (entity.distanceToSqr(target) <= ATTACK_RANGE * ATTACK_RANGE) {
                    // try to attack the target
                    entity.doHurtTarget(target)
                } else {
                    // add target to LookWalkTarget memory
                    addLookWalkTargets(entity)
                }

                // reset last known location of target
                lastKnownLocation = Optional.of(target.blockPosition())

                // Reset search time when target is visible
                searchTime = 0
            } else {
                // begin search for new target
                startSearching(entity)
            }
            // if player is not present or unable to be seen by the entity then perform a search
        } else if (searching) {
            performSearch(entity)
        } else {
            targetEntity = findPlayer(world, entity)
        }
    }

    private fun startSearching(entity: LivingEntity) {
        // if there is a last known location of the player
        if (lastKnownLocation.isPresent) {
            searching = true
            searchTime = 0
            entity.brain.setMemory(MemoryModuleType.WALK_TARGET, WalkTarget(lastKnownLocation.get(), WALK_SPEED, 1))
        }
    }

    private fun performSearch(entity: LivingEntity) {
        if (!lastKnownLocation.isPresent) {
            searching = false
            return
        }

        searchTime++
        if (searchTime % 20 == 0) {
            // Move to a new random position within a radius around the last known location
            val randomPos = lastKnownLocation.get().offset(
                    entity.random.nextInt(SEARCH_RADIUS * 2) - SEARCH_RADIUS,
                    0,
                    entity.random.nextInt(SEARCH_RADIUS * 2) - SEARCH_RADIUS
            )
            entity.brain.setMemory(MemoryModuleType.WALK_TARGET, WalkTarget(randomPos, WALK_SPEED, 1))
        }
        if (searchTime >= MAX_SEARCH_DURATION) {
            searching = false
            targetEntity = findPlayer(entity.level() as ServerLevel, entity)
        }

        // while searching listen for any disturbances to be used even
        val disturbanceLocation = entity.brain.getMemory(MemoryModuleType.DISTURBANCE_LOCATION)
        disturbanceLocation.ifPresent { onEntityHeard(entity, it) }
    }

    fun onEntityHeard(entity: LivingEntity, pos: BlockPos) {
        if (searching && !targetEntity.isPresent) {
            searchTime = 0
            lastKnownLocation = Optional.of(pos)
            entity.brain.setMemory(MemoryModuleType.WALK_TARGET, WalkTarget(pos, WALK_SPEED, 1))
        }
    }
}
