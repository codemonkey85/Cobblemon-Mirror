/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.abilities

import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.abilities.*
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder

/**
 * Crappy Pokémon feature
 *
 * @author Hiroku
 * @since July 28th, 2022
 */
class HiddenAbility(private val templateHolder: Holder<AbilityTemplate>) : PotentialAbility {

    override val priority: Priority = Priority.LOW
    override val type = PotentialAbilityType.HIDDEN
    override val template: AbilityTemplate = this.templateHolder.value()
    override fun isSatisfiedBy(aspects: Set<String>) = false // TODO: actually implement hidden abilities ig? Chance in config or aspect check?

    companion object {
        @JvmStatic
        val CODEC: MapCodec<HiddenAbility> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                AbilityTemplate.CODEC.fieldOf("ability").forGetter(HiddenAbility::templateHolder)
            ).apply(instance, ::HiddenAbility)
        }
    }
}