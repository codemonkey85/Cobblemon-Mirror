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
import java.util.*
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.ai.behavior.Behavior
import net.minecraft.world.entity.ai.behavior.BlockPosTracker
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.minecraft.world.entity.ai.memory.WalkTarget
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.CampfireBlock
import net.minecraft.world.level.block.CandleBlock
import net.minecraft.world.level.block.CandleCakeBlock
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.gameevent.GameEvent

//todo(broccoli): add a cooldown
class IgniteTask : Behavior<PokemonEntity>(
    ImmutableMap.of(
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
        this.pos = findIgnitionPos(world, entity)
        return pos.isPresent
    }

    override fun canStillUse(world: ServerLevel, entity: PokemonEntity, time: Long): Boolean {
        return duration < MAX_DURATION && pos.isPresent
    }

    private fun findIgnitionPos(world: ServerLevel, entity: PokemonEntity): Optional<BlockPos> {
        val mutable = BlockPos.MutableBlockPos()
        var optional = Optional.empty<BlockPos>()
        var i = 0

        //todo(broccoli): cleanup
        for (j in -1..1) {
            for (k in -1..1) {
                for (l in -1..1) {
                    mutable.set(j, k, l)
                    if (canLight(mutable, world)) {
                        ++i
                        if (world.random.nextInt(i) == 0) {
                            optional = Optional.of(mutable.immutable())
                        }
                    }
                }
            }
        }

        return optional
    }

    private fun canLight(pos: BlockPos, world: ServerLevel): Boolean {
        val blockState = world.getBlockState(pos)
        val block = blockState.block

        return block is CampfireBlock && !blockState.getValue(CampfireBlock.LIT)
    }

    override fun start(world: ServerLevel, entity: PokemonEntity, time: Long) {
        addLookWalkTargets(entity)
        startTime = time
        duration = 0
    }

    private fun addLookWalkTargets(entity: PokemonEntity) {
        pos.ifPresent { pos: BlockPos? ->
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
        val blockState = world.getBlockState(blockPos)

        if (time >= this.startTime && blockPos.closerToCenterThan(entity.position(), 1.0)) {
            if (CampfireBlock.canLight(blockState) || CandleBlock.canLight(blockState) || CandleCakeBlock.canLight(blockState) && !world.isRainingAt(blockPos)) {
                world.playSound(null, blockPos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, world.getRandom().nextFloat() * 0.4f + 0.8f)
                world.gameEvent(entity, GameEvent.BLOCK_CHANGE, blockPos)
                world.setBlock(blockPos, (blockState.setValue(BlockStateProperties.LIT, true)), 11)
                pos = this.findIgnitionPos(world, entity)
                addLookWalkTargets(entity)
                startTime = time + 40L
            }

            ++duration
        }
    }

    private fun createBoneMealStack() = ItemStack(Items.BONE_MEAL, 1)
}