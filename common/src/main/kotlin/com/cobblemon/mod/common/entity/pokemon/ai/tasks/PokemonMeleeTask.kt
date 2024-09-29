/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.tasks

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.ai.behavior.EntityTracker
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType

object PokemonMeleeTask {
    fun create(cooldownBetweenAttacks: Int): OneShot<PokemonEntity> {
        return BehaviorBuilder.create { context ->
            context.group(
                context.registered(MemoryModuleType.LOOK_TARGET),
                context.present(MemoryModuleType.ATTACK_TARGET),
                context.absent(MemoryModuleType.ATTACK_COOLING_DOWN),
                context.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
            ).apply(context) { lookTarget, attackTarget, attackCooldown, visibleMobs ->
                Trigger { world, entity, _ ->
                    val livingEntity = context.get(attackTarget)
                    if (entity.isWithinMeleeAttackRange(livingEntity) && context.get(visibleMobs).contains(livingEntity)) {
                        lookTarget.set(EntityTracker(livingEntity, true))
                        entity.swing(InteractionHand.MAIN_HAND)
                        entity.doHurtTarget(livingEntity)
                        attackCooldown.setWithExpiry(true, cooldownBetweenAttacks.toLong())
                        return@Trigger true
                    } else {
                        return@Trigger false
                    }
                }
            }
        }
    }
}