/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.variants

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.evolution.ContextEvolution
import com.cobblemon.mod.common.api.pokemon.evolution.EvolutionType
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirement
import com.cobblemon.mod.common.api.tags.RegistryBasedCondition
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.codec.CodecUtils
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.HolderSet
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block

/**
 * Represents a [ContextEvolution] with [RegistryLikeCondition] of type [Block] context.
 * These are triggered upon interaction with any [Block] that matches the given context.
 *
 * @property requiredContext The [RegistryLikeCondition] of type [Block] expected to match.
 * @author Licious
 * @since October 31st, 2022
 */
open class BlockClickEvolution(
    override val id: String,
    override val result: PokemonProperties,
    override val shedder: PokemonProperties?,
    override val requiredContext: RegistryBasedCondition<Block>,
    override var optional: Boolean,
    override var consumeHeldItem: Boolean,
    override val requirements: Set<EvolutionRequirement>,
    override val learnableMoves: HolderSet<MoveTemplate>
) : ContextEvolution<BlockClickEvolution.BlockInteractionContext, RegistryBasedCondition<Block>> {

    override fun testContext(pokemon: Pokemon, context: BlockInteractionContext): Boolean {
        return this.requiredContext.fits(context.block, context.world.registryAccess().registryOrThrow(Registries.BLOCK))
    }

    override val type: EvolutionType<*> = EvolutionType.BLOCK_CLICK

    data class BlockInteractionContext(
        val block: Block,
        val world: Level
    )

    companion object {

        @JvmStatic
        val CODEC: MapCodec<BlockClickEvolution> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.STRING.fieldOf("id").forGetter(BlockClickEvolution::id),
                PokemonProperties.CODEC.fieldOf("result").forGetter(BlockClickEvolution::result),
                PokemonProperties.CODEC.optionalFieldOf("result", null).forGetter(BlockClickEvolution::shedder),
                RegistryBasedCondition.codec(Registries.BLOCK).fieldOf("requiredContext").forGetter(BlockClickEvolution::requiredContext),
                Codec.BOOL.optionalFieldOf("optional", true).forGetter(BlockClickEvolution::optional),
                Codec.BOOL.optionalFieldOf("consumeHeldItem", true).forGetter(BlockClickEvolution::consumeHeldItem),
                CodecUtils.setOf(EvolutionRequirement.CODEC).fieldOf("requirements").forGetter(BlockClickEvolution::requirements),
                MoveTemplate.LIST_CODEC.fieldOf("learnableMoves").forGetter(BlockClickEvolution::learnableMoves),
            ).apply(instance, ::BlockClickEvolution)
        }

    }
}