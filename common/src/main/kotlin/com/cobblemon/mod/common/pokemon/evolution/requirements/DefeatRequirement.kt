/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.requirements

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirement
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirementType
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.evolution.progress.DefeatEvolutionProgress
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.ExtraCodecs

/**
 * An [EvolutionRequirement] which requires a specific [amount] of [target]s defeated to pass.
 *
 * @param target The [PokemonProperties] of the target Pokémon.
 * @param amount The amount of [target]s to faint.
 *
 * @author Licious
 * @since January 28th, 2022
 */
class DefeatRequirement(val target: PokemonProperties, val amount: Int) : EvolutionRequirement {

    override fun check(pokemon: Pokemon): Boolean = pokemon.evolutionProxy.current()
        .progress()
        .filterIsInstance<DefeatEvolutionProgress>()
        .any { progress -> progress.currentProgress().target.originalString.equals(this.target.originalString, true) && progress.currentProgress().amount >= this.amount }

    override val type: EvolutionRequirementType<*> = EvolutionRequirementType.DEFEAT

    companion object {
        @JvmStatic
        val CODEC: MapCodec<DefeatRequirement> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                PokemonProperties.CODEC.fieldOf("target").forGetter(DefeatRequirement::target),
                ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(DefeatRequirement::amount),
            ).apply(instance, ::DefeatRequirement)
        }
    }

}