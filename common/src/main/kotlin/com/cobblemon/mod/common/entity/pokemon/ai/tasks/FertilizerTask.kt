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
import com.google.common.collect.ImmutableMap
import java.util.*
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.ai.behavior.Behavior
import net.minecraft.world.entity.ai.behavior.BlockPosTracker
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.minecraft.world.entity.ai.memory.WalkTarget
import net.minecraft.world.item.BoneMealItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

//todo(broccoli): add a cooldown
class FertilizerTask : Behavior<PokemonEntity>(
        ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        )
) {

    companion object {
        private const val MAX_DURATION = 80
    }

    private var startTime: Long = 0
    private var lastEndEntityAge: Long = 0
    private var duration = 0
    private var pos = Optional.empty<BlockPos>()

    override fun checkExtraStartConditions(world: ServerLevel, entity: PokemonEntity): Boolean {
        this.pos = entity.brain.getMemory(CobblemonMemories.NEARBY_GROWABLE_CROPS)
        return pos.isPresent
    }

    override fun canStillUse(world: ServerLevel, entity: PokemonEntity, time: Long): Boolean {
        return duration < MAX_DURATION && pos.isPresent
    }

    override fun start(world: ServerLevel, entity: PokemonEntity, time: Long) {
        addLookWalkTargets(entity)
        startTime = time
        duration = 0
    }

    private fun addLookWalkTargets(entity: PokemonEntity) {
        pos.ifPresent { pos: BlockPos ->
            val blockPosLookTarget = BlockPosTracker(pos)
            entity.brain.setMemory(
                    MemoryModuleType.LOOK_TARGET,
                    blockPosLookTarget
            )
            entity.brain.setMemory(
                    MemoryModuleType.WALK_TARGET,
                    WalkTarget(blockPosLookTarget, 0.5f, 1)
            )
        }
    }

    override fun stop(world: ServerLevel, entity: PokemonEntity, time: Long) {
        lastEndEntityAge = entity.age.toLong()
    }

    override fun tick(world: ServerLevel, entity: PokemonEntity, time: Long) {
        val blockPos = pos.get()

        if (time >= this.startTime && blockPos.closerThan(entity.blockPosition(), 1.0)) {
            if (BoneMealItem.growCrop(createBoneMealStack(), world, blockPos)) {
                world.globalLevelEvent(1505, blockPos, 0)
                pos = entity.brain.getMemory(CobblemonMemories.NEARBY_GROWABLE_CROPS)
                addLookWalkTargets(entity)
                startTime = time + 40L
            }

            ++duration
        }
    }

    private fun createBoneMealStack() = ItemStack(Items.BONE_MEAL, 1)
}