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
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.behavior.Behavior
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.minecraft.world.entity.ai.memory.WalkTarget
import net.minecraft.world.entity.ai.targeting.TargetingConditions

class DefendOwnerTask : Behavior<PokemonEntity>(
        ImmutableMap.of(
                CobblemonMemories.NEAREST_VISIBLE_ATTACKER, MemoryStatus.VALUE_PRESENT
        )
) {

    private var target: LivingEntity? = null
    private val targetPredicate = TargetingConditions.forCombat()

    override fun checkExtraStartConditions(world: ServerLevel, entity: PokemonEntity): Boolean {
        val nearestAttacker = entity.brain.getMemory(CobblemonMemories.NEAREST_VISIBLE_ATTACKER)
        if (nearestAttacker != null) {
            return nearestAttacker.isPresent
        }
        return false
    }

    override fun start(world: ServerLevel, entity: PokemonEntity, time: Long) {
        target = entity.brain.getMemory(CobblemonMemories.NEAREST_VISIBLE_ATTACKER)?.get()
        entity.target = target
    }

    override fun canStillUse(world: ServerLevel, entity: PokemonEntity, time: Long): Boolean {
        return target != null && target!!.isAlive && entity.canAttack(target!!, targetPredicate)
    }

    override fun tick(world: ServerLevel, entity: PokemonEntity, time: Long) {
        target?.let {
            entity.target = it
            val followRange = entity.getAttribute(Attributes.FOLLOW_RANGE)?.value

            if (followRange != null && entity.distanceTo(it) <= followRange) {
                if (entity.distanceTo(it) > 2.0) {
                    entity.brain.setMemory(
                        MemoryModuleType.WALK_TARGET,
                        WalkTarget(it, 0.6f, 1)
                    )
                } else {
                    entity.doHurtTarget(it)
                }
            }
        }
    }

    override fun stop(world: ServerLevel, entity: PokemonEntity, time: Long) {
        target = null
        entity.target = null
        entity.brain.eraseMemory(MemoryModuleType.WALK_TARGET)
    }
}
