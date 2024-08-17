/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.sensors

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import java.util.Optional
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.Sensor
import net.minecraft.world.level.block.CropBlock

class PokemonGrowableCropSensor : Sensor<PokemonEntity>() {
    override fun doTick(world: ServerLevel, entity: PokemonEntity) {
        findBoneMealPos(world, entity).ifPresent {
            entity.brain.setMemory(CobblemonMemories.NEARBY_GROWABLE_CROPS, it)
        }
    }

    override fun requires(): Set<MemoryModuleType<*>> {
        return setOf(CobblemonMemories.NEARBY_GROWABLE_CROPS)
    }

    private fun findBoneMealPos(world: ServerLevel, entity: PokemonEntity): Optional<BlockPos> {
        val mutable = BlockPos.MutableBlockPos()
        var optional = Optional.empty<BlockPos>()
        var i = 0

        //todo(broccoli): cleanup
        for (j in -1..1) {
            for (k in -1..1) {
                for (l in -1..1) {
                    mutable.set(j, k, l)
                    if (canBoneMeal(mutable, world)) {
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

    private fun canBoneMeal(pos: BlockPos, world: ServerLevel): Boolean {
        val blockState = world.getBlockState(pos)
        val block = blockState.block

        return block is CropBlock && !block.isMaxAge(blockState)
    }
}