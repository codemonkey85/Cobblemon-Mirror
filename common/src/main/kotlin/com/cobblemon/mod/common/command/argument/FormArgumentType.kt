/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command.argument

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Species
import com.google.common.base.Splitter
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import java.util.StringTokenizer
import java.util.concurrent.CompletableFuture

/**
 * An [ArgumentType] for form data
 *
 * REQUIRES A [SpeciesArgumentType] BEFORE IT IN THE COMMAND
 *
 * @author Apion
 * @since February 26, 2024
 */
class FormArgumentType : ArgumentType<FormData> {
    override fun parse(reader: StringReader): FormData {
        var speciesBeginning = reader.cursor - 2
        val formTokenBeginning = reader.cursor - 1
        val formToken = reader.readString()
        while (reader.string[speciesBeginning] != ' ') {
            speciesBeginning--
        }
        speciesBeginning++
        val speciesToken = reader.string.substring(speciesBeginning, formTokenBeginning)
        val species = PokemonSpecies.getByName(speciesToken) ?: PokemonSpecies.species.first()
        val result = species.forms.union(listOf(species.standardForm)).first { it.formOnlyShowdownId().lowercase() == formToken }
        return result
    }

    override fun <S : Any?> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return CompletableFuture.supplyAsync {
            val formToken = builder.remaining

            var curIdx = builder.start - 2
            while (builder.input[curIdx] != ' ') {
                curIdx--
            }
            curIdx++
            val speciesToken = builder.input.substring(curIdx, builder.start - 1)

            val species = PokemonSpecies.getByName(speciesToken) ?: PokemonSpecies.species.first()
            val forms = species.forms.union(listOf(species.standardForm)).filter { it.formOnlyShowdownId().lowercase().startsWith(formToken) }
            forms.forEach { str ->
                builder.suggest(str.formOnlyShowdownId())
            }
            return@supplyAsync builder.build()
        }
    }

    companion object {
        fun form() = FormArgumentType()
    }
}