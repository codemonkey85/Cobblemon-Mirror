/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.requirements

import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.evolution.requirements.template.EntityQueryRequirement
import net.minecraft.world.entity.LivingEntity

class WeatherRequirement : EntityQueryRequirement {
    companion object {
        const val ADAPTER_VARIANT = "weather"
    }

    val isRaining: Boolean? = null
    val isThundering: Boolean? = null

    override fun check(pokemon: Pokemon, queriedEntity: LivingEntity): Boolean {
        val world = queriedEntity.level()
        return when {
            isRaining == true && !world.isRaining -> false
            isRaining == false && world.isRaining -> false
            isThundering == true && !world.isThundering -> false
            isThundering == false && world.isThundering -> false
            else -> true
        }
    }
}