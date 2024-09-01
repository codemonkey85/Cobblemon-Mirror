package com.cobblemon.mod.common.command.argument

import com.cobblemon.mod.common.api.pokedex.Dexes
import com.cobblemon.mod.common.api.pokedex.def.PokedexDef
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import java.util.concurrent.CompletableFuture

class DexArgumentType : ArgumentType<PokedexDef> {
    override fun parse(reader: StringReader): PokedexDef {
        val dexToken = reader.readString()
        //Using first here (when we only check path) is mildly concerning, but hopefully there wont be name collisions
        val dex = Dexes.dexEntryMap.keys.filter { it.path == dexToken }.first()
        return Dexes.dexEntryMap[dex]!!
    }

    override fun <S : Any?> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return CompletableFuture.supplyAsync {
            val dexToken = builder.remaining
            val matchingDexes = Dexes.dexEntryMap.keys
                .filter { it.path.startsWith(dexToken) }
                .map { it.path }
                .forEach { builder.suggest(it) }
            return@supplyAsync builder.build()
        }
    }

    companion object {
        fun dex() = DexArgumentType()
    }

}