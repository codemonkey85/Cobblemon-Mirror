/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command.argument

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.commandLang
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.server.level.ServerPlayer
import java.util.concurrent.CompletableFuture

class PartySlotArgumentType : ArgumentType<Int> {

    override fun parse(reader: StringReader): Int {
        val slot = reader.readInt()
        return when {
            slot < MIN -> throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooLow().createWithContext(reader, slot, MIN)
            slot > MAX -> throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooHigh().createWithContext(reader, slot, MAX)
            else -> slot
        }
    }

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> =
        SharedSuggestionProvider.suggest(EXAMPLES, builder)

    override fun getExamples() = EXAMPLES

    companion object {

        private const val MIN = 1
        private const val MAX = 6
        private val EXAMPLES = (MIN..MAX).map { it.toString() }
        private val INVALID_SLOT = DynamicCommandExceptionType { slot -> commandLang("general.invalid-party-slot", slot).red() }

        fun partySlot() = PartySlotArgumentType()

        fun <S> getPokemon(context: CommandContext<S>, name: String): Pokemon {
            val slot = context.getArgument(name, Int::class.java)
            val source = context.source as? CommandSourceStack ?: throw CommandSourceStack.ERROR_NOT_PLAYER.create()
            val player = source.entity as? ServerPlayer ?: throw CommandSourceStack.ERROR_NOT_PLAYER.create()
            val party = Cobblemon.storage.getParty(player)
            return party.get(slot - 1) ?: throw INVALID_SLOT.create(slot)
        }

        fun <S> getPokemonOf(context: CommandContext<S>, name: String, player: ServerPlayer): Pokemon {
            val slot = context.getArgument(name, Int::class.java)
            val party = Cobblemon.storage.getParty(player)
            return party.get(slot - 1) ?: throw INVALID_SLOT.create(slot)
        }
    }

}