/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.npc.ai

import com.bedrockk.molang.Expression
import com.bedrockk.molang.runtime.struct.QueryStruct
import com.cobblemon.mod.common.api.ai.config.task.TaskConfig.Companion.runtime
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.resolveFloat
import com.cobblemon.mod.common.util.resolveInt
import com.cobblemon.mod.common.util.withQueryValue
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType

object MeleeAttackTask {
    fun create(range: Expression, cooldownTicks: Expression): OneShot<LivingEntity> = BehaviorBuilder.create {
        it.group(
            it.present(MemoryModuleType.ATTACK_TARGET),
            it.registered(MemoryModuleType.ATTACK_COOLING_DOWN)
        ).apply(it) { attackTarget, cooldown ->
            Trigger { world, entity, _ ->
                runtime.withQueryValue("entity", (entity as? PosableEntity)?.struct ?: QueryStruct(hashMapOf()))
                val range = runtime.resolveFloat(range)
                val cooldownTicks = runtime.resolveInt(cooldownTicks)

                val attackTarget = it.get(attackTarget)
                if (entity.distanceTo(attackTarget) <= range) {
                    entity.doHurtTarget(attackTarget)
                    cooldown.setWithExpiry(true, cooldownTicks.toLong())
                    true
                } else {
                    false
                }
            }
        }
    }
}