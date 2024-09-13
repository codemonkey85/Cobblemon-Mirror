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
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.pokemon.Pokemon
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder

class MoveTypeRequirement(val elementalType: Holder<ElementalType>) : EvolutionRequirement {

    override fun check(pokemon: Pokemon): Boolean = this.elementalType.unwrap().map(
        { id -> pokemon.moveSet.getMoves().any { move -> move.template.type.resourceKey() == id } },
        { type -> pokemon.moveSet.getMoves().any { move -> move.template.type == type } },
    )

    override val type: EvolutionRequirementType<*> = EvolutionRequirementType.HAS_MOVE_TYPE

    companion object {
        @JvmStatic
        val CODEC: MapCodec<MoveTypeRequirement> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ElementalType.CODEC.fieldOf("elementalType").forGetter(MoveTypeRequirement::elementalType),
            ).apply(instance, ::MoveTypeRequirement)
        }
    }

}