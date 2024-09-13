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
import com.cobblemon.mod.common.pokemon.evolution.progress.DamageTakenEvolutionProgress
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.ExtraCodecs

/**
 * An [EvolutionRequirement] which requires a specific [amount] of damage taken in battle without fainting in order to pass.
 * It keeps track of progress through [DamageTakenRequirement].
 *
 * @param amount The requirement amount of damage.
 *
 * @author Licious
 * @since January 27th, 2022
 */
class DamageTakenRequirement(val amount: Int) : EvolutionRequirement {

    override fun check(pokemon: Pokemon): Boolean = pokemon.evolutionProxy.current()
        .progress()
        .filterIsInstance<DamageTakenEvolutionProgress>()
        .any { progress -> progress.currentProgress().amount >= this.amount }

    override val type: EvolutionRequirementType<*> = EvolutionRequirementType.DAMAGE_TAKEN

    companion object {
        @JvmStatic
        val CODEC: MapCodec<DamageTakenRequirement> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(DamageTakenRequirement::amount)
            ).apply(instance, ::DamageTakenRequirement)
        }
    }

}