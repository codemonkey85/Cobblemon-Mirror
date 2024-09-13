/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.requirements

import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirement
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirementType
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.evolution.progress.UseMoveEvolutionProgress
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import java.util.function.Function

/**
 * An [EvolutionRequirement] meant to require a move to have been used a specific amount of times.
 *
 * @param move The [MoveTemplate] expected to be used.
 * @param amount The amount of times it has been used.
 *
 * @author Licious
 * @since January 25th, 2023
 */
class UseMoveRequirement(val move: Holder<MoveTemplate>, val amount: Int) : EvolutionRequirement {

    override fun check(pokemon: Pokemon): Boolean {
        val moveInstance = move.unwrap().map(Moves::get) { it } ?: return false
        return pokemon.evolutionProxy.current()
            .progress()
            .filterIsInstance<UseMoveEvolutionProgress>()
            .any { progress -> progress.currentProgress().move == moveInstance && progress.currentProgress().amount >= this.amount }
    }

    override val type: EvolutionRequirementType<*> = EvolutionRequirementType.USE_MOVE

    companion object {

        @JvmStatic
        val CODEC: MapCodec<UseMoveRequirement> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                MoveTemplate.CODEC.fieldOf("move").forGetter(UseMoveRequirement::move),
                Codec.INT.fieldOf("amount").forGetter(UseMoveRequirement::amount),
            ).apply(instance, ::UseMoveRequirement)
        }

    }

}