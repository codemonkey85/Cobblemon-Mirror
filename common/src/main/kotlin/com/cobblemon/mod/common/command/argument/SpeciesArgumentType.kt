/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command.argument

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import java.util.concurrent.CompletableFuture

//Very helpful for all command related stuff: https://fabricmc.net/wiki/tutorial:commands#brigadier_explained
class SpeciesArgumentType : ArgumentType<Species> {

    companion object {
        val EXAMPLES: List<String> = listOf("eevee")
        val INVALID_POKEMON = Component.translatable("cobblemon.command.pokespawn.invalid-pokemon")

        fun species() = SpeciesArgumentType()

        fun <S> getPokemon(context: CommandContext<S>, name: String): Species {
            return context.getArgument(name, Species::class.java)
        }
    }

    override fun parse(reader: StringReader): Species {
        return PokemonSpecies.getByIdentifier(reader.asIdentifierDefaultingNamespace())
            ?: throw SimpleCommandExceptionType(INVALID_POKEMON).createWithContext(reader)
    }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        // Just a neat shortcut for our own Pokémon since that will be the most common setup no need to overcomplicate
        return SharedSuggestionProvider.suggest(PokemonSpecies.species.map { if (it.resourceIdentifier.namespace == Cobblemon.MODID) it.resourceIdentifier.path else it.resourceIdentifier.toString() }, builder)
    }

    override fun getExamples() = EXAMPLES
}