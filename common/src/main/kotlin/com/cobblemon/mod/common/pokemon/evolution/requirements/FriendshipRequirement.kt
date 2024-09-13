/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.requirements

import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirement
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirementType
import com.cobblemon.mod.common.pokemon.Pokemon
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.ExtraCodecs

/**
 * An [EvolutionRequirement] for when a certain amount of [Pokemon.friendship] is expected.
 *
 * @property amount The required [Pokemon.friendship] amount, expects between 0 & 255.
 * @author Licious
 * @since March 21st, 2022
 */
class FriendshipRequirement(val amount: Int) : EvolutionRequirement {

    override fun check(pokemon: Pokemon) = pokemon.friendship >= this.amount

    override val type: EvolutionRequirementType<*> = EvolutionRequirementType.FRIENDSHIP

    companion object {
        @JvmStatic
        val CODEC: MapCodec<FriendshipRequirement> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(FriendshipRequirement::amount)
            ).apply(instance, ::FriendshipRequirement)
        }
    }
}