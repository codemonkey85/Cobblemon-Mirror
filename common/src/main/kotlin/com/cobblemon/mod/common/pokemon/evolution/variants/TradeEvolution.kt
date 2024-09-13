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
import com.cobblemon.mod.common.api.pokemon.evolution.ContextEvolution
import com.cobblemon.mod.common.api.pokemon.evolution.EvolutionType
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirement
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.codec.CodecUtils
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.HolderSet

/**
 * Represents a [ContextEvolution] with [Pokemon] context.
 * This is triggered by trading.
 * The context is the received [Pokemon] from the trade.
 *
 * @property requiredContext The [PokemonProperties] representation of the expected received [Pokemon] from the trade.
 * @author Licious
 * @since March 20th, 2022
 */
open class TradeEvolution(
    override val id: String,
    override val result: PokemonProperties,
    override val shedder: PokemonProperties?,
    override val requiredContext: PokemonProperties,
    override var optional: Boolean,
    override var consumeHeldItem: Boolean,
    override val requirements: Set<EvolutionRequirement>,
    override val learnableMoves: HolderSet<MoveTemplate>
) : ContextEvolution<Pokemon, PokemonProperties> {

    override fun testContext(pokemon: Pokemon, context: Pokemon) = this.requiredContext.matches(context)

    override val type: EvolutionType<*> = EvolutionType.TRADE

    companion object {

        @JvmStatic
        val CODEC: MapCodec<TradeEvolution> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.STRING.fieldOf("id").forGetter(TradeEvolution::id),
                PokemonProperties.CODEC.fieldOf("result").forGetter(TradeEvolution::result),
                PokemonProperties.CODEC.optionalFieldOf("result", null).forGetter(TradeEvolution::shedder),
                PokemonProperties.CODEC.fieldOf("requiredContext").forGetter(TradeEvolution::requiredContext),
                Codec.BOOL.optionalFieldOf("optional", true).forGetter(TradeEvolution::optional),
                Codec.BOOL.optionalFieldOf("consumeHeldItem", true).forGetter(TradeEvolution::consumeHeldItem),
                CodecUtils.setOf(EvolutionRequirement.CODEC).fieldOf("requirements").forGetter(TradeEvolution::requirements),
                MoveTemplate.LIST_CODEC.fieldOf("learnableMoves").forGetter(TradeEvolution::learnableMoves),
            ).apply(instance, ::TradeEvolution)
        }

    }
}