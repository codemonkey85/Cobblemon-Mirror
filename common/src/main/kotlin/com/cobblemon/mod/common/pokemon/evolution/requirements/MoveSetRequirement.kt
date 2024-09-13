/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.requirements

import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirement
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirementType
import com.cobblemon.mod.common.pokemon.Pokemon
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder

/**
 * An [EvolutionRequirement] for when a certain [MoveTemplate] is expected in the [Pokemon.moveSet].
 *
 * @property move The required [MoveTemplate].
 * @author Licious
 * @since March 21st, 2022
 */
class MoveSetRequirement(val move: Holder<MoveTemplate>) : EvolutionRequirement {

    override fun check(pokemon: Pokemon): Boolean = this.move.unwrap().map(
        { id -> pokemon.moveSet.getMoves().any { move -> move.template.resourceKey() == id } },
        { template -> pokemon.moveSet.getMoves().any { move -> move.template == template } },
    )

    override val type: EvolutionRequirementType<*> = EvolutionRequirementType.HAS_MOVE

    companion object {
        @JvmStatic
        val CODEC: MapCodec<MoveSetRequirement> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                MoveTemplate.CODEC.fieldOf("move").forGetter(MoveSetRequirement::move),
            ).apply(instance, ::MoveSetRequirement)
        }
    }
}