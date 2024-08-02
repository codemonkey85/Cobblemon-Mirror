/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command.argument

import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.party
import com.cobblemon.mod.common.util.pc
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.StringRepresentableArgument
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.StringRepresentable

class PokemonStoreArgumentType : StringRepresentableArgument<StoreType>(StoreType.CODEC, StoreType::values) {
    companion object {
        fun pokemonStore() = PokemonStoreArgumentType()
        fun pokemonStoreFrom(context: CommandContext<CommandSourceStack>, id: String): StoreType = context.getArgument(id, StoreType::class.java)
    }
}

enum class StoreType(val storeFetcher: (ServerPlayer) -> Collection<Pokemon>) : StringRepresentable {

    PARTY({ player -> player.party().filterNotNull() }),
    PC({ player -> player.pc().filterNotNull() }),
    ALL({ player -> PARTY.storeFetcher(player) + PC.storeFetcher(player) });

    override fun getSerializedName(): String = name.lowercase()

    companion object {
        val CODEC = StringRepresentable.fromEnum(::values)
    }

}