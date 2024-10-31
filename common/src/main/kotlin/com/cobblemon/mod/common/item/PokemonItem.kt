/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.item.components.PokemonItemComponent
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.RenderablePokemon
import com.cobblemon.mod.common.pokemon.Species
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import org.joml.Vector4f

class PokemonItem : CobblemonItem(Properties().stacksTo(1).component(CobblemonItemComponents.POKEMON_ITEM, null)) {

    override fun getName(stack: ItemStack): Component = this.species(stack)?.translatedName ?: super.getName(stack)

    fun asPokemon(stack: ItemStack): Pokemon? {
        val species = this.species(stack) ?: return null
        val aspects = this.aspects(stack) ?: setOf()
        return Pokemon().apply {
            this.species = species
            this.forcedAspects = aspects
        }
    }

    fun getSpeciesAndAspects(stack: ItemStack): Pair<Species, Set<String>>? {
        return (species(stack) ?: return null) to (aspects(stack) ?: setOf())
    }

    fun asRenderablePokemon(stack: ItemStack): RenderablePokemon? = this.asPokemon(stack)?.asRenderablePokemon()

    private fun species(stack: ItemStack): Species? {
        return stack.get(CobblemonItemComponents.POKEMON_ITEM)?.species?.let(PokemonSpecies::getByIdentifier)
    }

    private fun aspects(stack: ItemStack): Set<String>? {
        return stack.get(CobblemonItemComponents.POKEMON_ITEM)?.aspects
    }

    fun tint(stack: ItemStack): Vector4f {
        return stack.get(CobblemonItemComponents.POKEMON_ITEM)?.tint ?: Vector4f(1f, 1f, 1f, 1f)
    }

    companion object {
        @JvmOverloads
        @JvmStatic
        fun from(pokemon: Pokemon, count: Int = 1, tint: Vector4f? = null): ItemStack = from(pokemon.species, pokemon.aspects, count, tint)

        @JvmOverloads
        @JvmStatic
        fun from(properties: PokemonProperties, count: Int = 1, tint: Vector4f? = null): ItemStack = from(properties.create(), count, tint)

        @JvmOverloads
        @JvmStatic
        fun from(species: Species, vararg aspects: String, count: Int = 1, tint: Vector4f? = null): ItemStack = from(species, aspects.toSet(), count, tint)

        @JvmStatic
        fun from(species: Species, aspects: Set<String>, count: Int = 1, tint: Vector4f? = null): ItemStack {
            val stack = ItemStack(CobblemonItems.POKEMON_MODEL, count)
            stack.set(CobblemonItemComponents.POKEMON_ITEM, PokemonItemComponent(species.resourceIdentifier, aspects, tint))
            return stack
        }
    }
}