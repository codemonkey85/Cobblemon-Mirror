/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command.argument

import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.Moves
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import java.util.concurrent.CompletableFuture

class MoveArgumentType: ArgumentType<MoveTemplate> {

    override fun parse(reader: StringReader): MoveTemplate = Moves.getByName(reader.readString()) ?: throw SimpleCommandExceptionType(INVALID_MOVE).createWithContext(reader)

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return SharedSuggestionProvider.suggest(Moves.names(), builder)
    }

    override fun getExamples() = EXAMPLES

    companion object {

        val EXAMPLES: List<String> = listOf("tackle")
        val INVALID_MOVE: MutableComponent = Component.translatable("cobblemon.command.pokespawn.invalid-move")

        fun move() = MoveArgumentType()

        fun <S> getMove(context: CommandContext<S>, name: String): MoveTemplate {
            return context.getArgument(name, MoveTemplate::class.java)
        }

    }

}