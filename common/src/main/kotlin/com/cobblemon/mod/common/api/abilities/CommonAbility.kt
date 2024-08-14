/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.abilities

import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.registry.CobblemonRegistries
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder

class CommonAbility(override val template: AbilityTemplate) : PotentialAbility {

    override val priority = Priority.LOWEST

    override val type = PotentialAbilityType.COMMON
    override fun isSatisfiedBy(aspects: Set<String>) = true

    companion object {
        @JvmStatic
        val CODEC: MapCodec<CommonAbility> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                CobblemonRegistries.ABILITY.byNameCodec().fieldOf("ability").forGetter(CommonAbility::template)
            ).apply(instance, ::CommonAbility)
        }
    }

}