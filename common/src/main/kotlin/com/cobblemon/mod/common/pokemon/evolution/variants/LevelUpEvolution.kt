/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.variants

import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.evolution.EvolutionType
import com.cobblemon.mod.common.api.pokemon.evolution.PassiveEvolution
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirement
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.codec.CodecUtils
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.HolderSet

/**
 * Represents a [PassiveEvolution].
 * This can be triggered at any check as long as the [Pokemon] passes [LevelUpEvolution.isValid].
 *
 * @property levels The level range the [Pokemon] is expected to be in, if the range only has a single number the [Pokemon.level] will need to be equal or greater then it instead.
 * @author Licious
 * @since March 20th, 2022
 */
open class LevelUpEvolution(
    override val id: String,
    override val result: PokemonProperties,
    override val shedder: PokemonProperties?,
    override var optional: Boolean,
    override var consumeHeldItem: Boolean,
    override val requirements: Set<EvolutionRequirement>,
    override val learnableMoves: HolderSet<MoveTemplate>,
    override val permanent: Boolean
) : PassiveEvolution {

    override val type: EvolutionType<*> = EvolutionType.LEVEL_UP

    companion object {
        @JvmStatic
        val CODEC: MapCodec<LevelUpEvolution> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.STRING.fieldOf("id").forGetter(LevelUpEvolution::id),
                PokemonProperties.CODEC.fieldOf("result").forGetter(LevelUpEvolution::result),
                PokemonProperties.CODEC.optionalFieldOf("result", null).forGetter(LevelUpEvolution::shedder),
                Codec.BOOL.optionalFieldOf("optional", true).forGetter(LevelUpEvolution::optional),
                Codec.BOOL.optionalFieldOf("consumeHeldItem", true).forGetter(LevelUpEvolution::consumeHeldItem),
                CodecUtils.setOf(EvolutionRequirement.CODEC).fieldOf("requirements").forGetter(LevelUpEvolution::requirements),
                MoveTemplate.LIST_CODEC.fieldOf("learnableMoves").forGetter(LevelUpEvolution::learnableMoves),
                Codec.BOOL.optionalFieldOf("permanent", false).forGetter(LevelUpEvolution::permanent),
            ).apply(instance, ::LevelUpEvolution)
        }

    }
}