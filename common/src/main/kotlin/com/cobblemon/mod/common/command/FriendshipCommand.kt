/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.command.argument.PartySlotArgumentType
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.commandLang
import com.cobblemon.mod.common.util.permission
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.server.level.ServerPlayer

object FriendshipCommand {

    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(Commands.literal("friendship")
            .permission(CobblemonPermissions.FRIENDSHIP)
            .then(
                Commands.argument("slot", PartySlotArgumentType.partySlot())
                    .executes { execute(it.source, it.source.playerOrException, PartySlotArgumentType.getPokemon(it, "slot")) }
            ))
    }

    private fun execute(source: CommandSourceStack, target: ServerPlayer, pokemon: Pokemon) : Int {
        source.sendSuccess({ commandLang("friendship", pokemon.getDisplayName(), pokemon.friendship) }, true)
        return Command.SINGLE_SUCCESS
    }

}