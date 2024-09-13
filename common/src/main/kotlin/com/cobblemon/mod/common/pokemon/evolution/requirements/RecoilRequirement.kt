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
import com.cobblemon.mod.common.pokemon.evolution.progress.RecoilEvolutionProgress
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.ExtraCodecs

/**
 * An [EvolutionRequirement] which requires a specific [amount] of recoil without fainting in order to pass.
 * It keeps track of progress through [RecoilEvolutionProgress].
 *
 * @param amount The requirement amount of recoil
 *
 * @author Licious
 * @since January 27th, 2022
 */
class RecoilRequirement(val amount: Int) : EvolutionRequirement {

    override fun check(pokemon: Pokemon): Boolean = pokemon.evolutionProxy.current()
        .progress()
        .filterIsInstance<RecoilEvolutionProgress>()
        .any { progress -> progress.currentProgress().recoil >= this.amount }

    override val type: EvolutionRequirementType<*> = EvolutionRequirementType.RECOIL

    companion object {
        @JvmStatic
        val CODEC: MapCodec<RecoilRequirement> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(RecoilRequirement::amount)
            ).apply(instance, ::RecoilRequirement)
        }
    }

}