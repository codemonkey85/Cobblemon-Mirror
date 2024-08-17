/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.tasks

import com.cobblemon.mod.common.api.storage.party.PartyStore
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import kotlin.math.abs
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.ai.behavior.EntityTracker
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.WalkTarget
import net.minecraft.world.level.pathfinder.PathType
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator

object MoveToOwnerTask {
    fun create(completionRange: Int, maxDistance: Float, teleportDistance: Float): OneShot<PokemonEntity> = BehaviorBuilder.create {
        it.group(
            it.registered(MemoryModuleType.WALK_TARGET),
            it.absent(MemoryModuleType.ANGRY_AT)
        ).apply(it) { walkTarget, _ ->
            Trigger { _, entity, _ ->
                val owner = entity.owner ?: return@Trigger false
                if (entity.pokemon.storeCoordinates.get()?.store !is PartyStore) {
                    return@Trigger false
                }
                if (entity.distanceTo(owner) > teleportDistance) {
                    if (tryTeleport(entity, owner)) {
                        entity.brain.eraseMemory(MemoryModuleType.LOOK_TARGET)
                        entity.brain.eraseMemory(MemoryModuleType.WALK_TARGET)
                    }
                    return@Trigger true
                } else if (entity.distanceTo(owner) > maxDistance && it.tryGet(walkTarget).isEmpty) {
                    entity.brain.setMemory(MemoryModuleType.LOOK_TARGET, EntityTracker(owner, true))
                    entity.brain.setMemory(MemoryModuleType.WALK_TARGET, WalkTarget(owner, 0.4F, completionRange))
                    return@Trigger true
                }
                return@Trigger false
            }
        }
    }

    private fun tryTeleport(entity: PokemonEntity, owner: Entity): Boolean {
        val blockPos = owner.blockPosition()
        for (i in 0..9) {
            val j = this.getRandomInt(entity.random, -3, 3)
            val k = this.getRandomInt(entity.random, -1, 1)
            val l = this.getRandomInt(entity.random, -3, 3)
            val succeeded = this.tryTeleportTo(entity, owner, blockPos.x + j, blockPos.y + k, blockPos.z + l)
            if (succeeded) {
                return true
            }
        }
        return false
    }

    private fun tryTeleportTo(entity: PokemonEntity, owner: Entity, x: Int, y: Int, z: Int): Boolean {
        if (abs(x.toDouble() - owner.x) < 2.0 && abs(z - owner.z) < 2.0) {
            return false
        } else if (!this.canTeleportTo(entity, BlockPos(x, y, z))) {
            return false
        } else {
            entity.moveTo(
                x.toDouble() + 0.5, y.toDouble(), z.toDouble() + 0.5,
                entity.yRot,
                entity.xRot
            )
            entity.navigation.stop()
            return true
        }
    }

    private fun canTeleportTo(entity: PokemonEntity, pos: BlockPos): Boolean {
        val pathNodeType = WalkNodeEvaluator.getPathTypeStatic(entity, pos.mutable())
        if (pathNodeType != PathType.WALKABLE) {
            return false
        } else {
            val blockPos = pos.subtract(entity.blockPosition())
            return entity.level().noCollision(entity, entity.boundingBox.move(blockPos))
        }
    }

    private fun getRandomInt(random: RandomSource, min: Int, max: Int): Int {
        return random.nextInt(max - min + 1) + min
    }
}