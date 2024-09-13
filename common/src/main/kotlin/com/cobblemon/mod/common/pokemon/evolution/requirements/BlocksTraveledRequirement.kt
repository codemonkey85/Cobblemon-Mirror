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
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.ExtraCodecs

/**
 * An [EvolutionRequirement] that requires a specific [amount] of [PokemonEntity.blocksTraveled] to pass.
 *
 * @param amount The amount of blocks the entity must have traversed.
 *
 * @author Licious
 * @since January 28th, 2023
 */
class BlocksTraveledRequirement(val amount: Int) : EvolutionRequirement {

    override fun check(pokemon: Pokemon): Boolean {
        val pokemonEntity = pokemon.entity ?: return false
        return pokemonEntity.blocksTraveled >= this.amount
    }

    override val type: EvolutionRequirementType<*> = EvolutionRequirementType.BLOCKS_TRAVELED

    companion object {
        @JvmStatic
        val CODEC: MapCodec<BlocksTraveledRequirement> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(BlocksTraveledRequirement::amount)
            ).apply(instance, ::BlocksTraveledRequirement)
        }
    }

}