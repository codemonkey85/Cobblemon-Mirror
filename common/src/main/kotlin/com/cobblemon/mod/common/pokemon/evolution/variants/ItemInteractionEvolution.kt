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
import com.cobblemon.mod.common.pokemon.evolution.predicate.NbtItemPredicate
import com.cobblemon.mod.common.util.codec.CodecUtils
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.HolderSet
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

/**
 * Represents a [ContextEvolution] with [NbtItemPredicate] context.
 * These are triggered upon interaction with any [ItemStack] that matches the given predicate.
 *
 * @property requiredContext The [NbtItemPredicate] expected to match.
 * @author Licious
 * @since March 20th, 2022
 */
open class ItemInteractionEvolution(
    override val id: String,
    override val result: PokemonProperties,
    override val shedder: PokemonProperties?,
    override val requiredContext: NbtItemPredicate,
    override var optional: Boolean,
    override var consumeHeldItem: Boolean,
    override val requirements: Set<EvolutionRequirement>,
    override val learnableMoves: HolderSet<MoveTemplate>,
) : ContextEvolution<ItemInteractionEvolution.ItemInteractionContext, NbtItemPredicate> {

    override fun testContext(pokemon: Pokemon, context: ItemInteractionContext): Boolean =
        this.requiredContext.item.fits(context.stack.item, context.world.registryAccess().registryOrThrow(Registries.ITEM))
        && this.requiredContext.nbt.map { it.matches(context.stack) }.orElse(true)

    override val type: EvolutionType<*> = EvolutionType.ITEM_INTERACTION

    data class ItemInteractionContext(
        val stack: ItemStack,
        val world: Level
    )

    companion object {

        @JvmStatic
        val CODEC: MapCodec<ItemInteractionEvolution> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.STRING.fieldOf("id").forGetter(ItemInteractionEvolution::id),
                PokemonProperties.CODEC.fieldOf("result").forGetter(ItemInteractionEvolution::result),
                PokemonProperties.CODEC.optionalFieldOf("result", null).forGetter(ItemInteractionEvolution::shedder),
                NbtItemPredicate.CODEC.fieldOf("requiredContext").forGetter(ItemInteractionEvolution::requiredContext),
                Codec.BOOL.optionalFieldOf("optional", true).forGetter(ItemInteractionEvolution::optional),
                Codec.BOOL.optionalFieldOf("consumeHeldItem", true).forGetter(ItemInteractionEvolution::consumeHeldItem),
                CodecUtils.setOf(EvolutionRequirement.CODEC).fieldOf("requirements").forGetter(ItemInteractionEvolution::requirements),
                MoveTemplate.LIST_CODEC.fieldOf("learnableMoves").forGetter(ItemInteractionEvolution::learnableMoves),
            ).apply(instance, ::ItemInteractionEvolution)
        }

    }
}