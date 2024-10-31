/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.requirements

import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirement
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.evolution.requirements.template.EntityQueryRequirement
import net.minecraft.world.entity.LivingEntity
import net.minecraft.server.level.ServerPlayer
import net.minecraft.resources.ResourceLocation

/**
 * An [EvolutionRequirement] that checks if the player has a certain [Advancement]
 *
 * @param requiredAdvancement The [ResourceLocation] of the required advancement
 *
 * @author whatsy
 */
class PlayerHasAdvancementRequirement(val requiredAdvancement: ResourceLocation) : EntityQueryRequirement {
    override fun check(pokemon: Pokemon, queriedEntity: LivingEntity): Boolean {
        val player = queriedEntity as? ServerPlayer ?: return false
        for (entry in player.advancements.progress) {
            if (entry.key.id == requiredAdvancement && entry.value.isDone) {
                return true
            }
        }
        return false
    }

    companion object {
        val ADAPTER_VARIANT = "advancement"
    }
}