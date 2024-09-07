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
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirement
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.registry.BlockIdentifierCondition
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
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
    override val requiredContext: RegistryLikeCondition<Block>,
    override var optional: Boolean,
    override var consumeHeldItem: Boolean,
    override val requirements: MutableSet<EvolutionRequirement>,
    override val learnableMoves: MutableSet<MoveTemplate>
) : ContextEvolution<BlockClickEvolution.BlockInteractionContext, RegistryLikeCondition<Block>> {
    constructor(): this(
        id = "id",
        result = PokemonProperties(),
        shedder = null,
        requiredContext = BlockIdentifierCondition(ResourceLocation.fromNamespaceAndPath("minecraft", "dirt")),
        optional = true,
        consumeHeldItem = true,
        requirements = mutableSetOf(),
        learnableMoves = mutableSetOf()
    )

    override fun testContext(pokemon: Pokemon, context: BlockInteractionContext): Boolean {
        return this.requiredContext.fits(context.block, context.world.registryAccess().registryOrThrow(Registries.BLOCK))
    }

    override fun equals(other: Any?) = other is BlockClickEvolution && other.id.equals(this.id, true)

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + ADAPTER_VARIANT.hashCode()
        return result
    }

    data class BlockInteractionContext(
        val block: Block,
        val world: Level
    )

    companion object {
        const val ADAPTER_VARIANT = "block_click"
    }
}