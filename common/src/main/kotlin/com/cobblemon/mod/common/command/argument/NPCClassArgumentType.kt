/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command.argument

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.npc.NPCClass
import com.cobblemon.mod.common.api.npc.NPCClasses
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.commandLang
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import java.util.concurrent.CompletableFuture
import net.minecraft.commands.SharedSuggestionProvider

class NPCClassArgumentType : ArgumentType<NPCClass> {
    companion object {
        val EXAMPLES: List<String> = listOf("cobblemon:example")
        val INVALID_NPC_CLASS = commandLang("error.nonpc")

        fun npcClass() = NPCClassArgumentType()

        fun <S> getNPCClass(context: CommandContext<S>, name: String): NPCClass {
            return context.getArgument(name, NPCClass::class.java)
        }
    }

    override fun parse(reader: StringReader): NPCClass {
        try {
            return reader.asIdentifierDefaultingNamespace().let { NPCClasses.getByIdentifier(it) } ?: throw Exception()
        } catch (e: Exception) {
            throw SimpleCommandExceptionType(INVALID_NPC_CLASS).createWithContext(reader)
        }
    }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return SharedSuggestionProvider.suggest(NPCClasses.classes.map { if (it.id.namespace == Cobblemon.MODID) it.id.path else it.id.toString() }, builder)
    }

    override fun getExamples() = EXAMPLES
}