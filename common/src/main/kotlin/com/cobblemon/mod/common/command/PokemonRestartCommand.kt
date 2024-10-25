/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreType
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreTypes
import com.cobblemon.mod.common.net.messages.client.SetClientPlayerDataPacket
import com.cobblemon.mod.common.net.messages.client.storage.party.InitializePartyPacket
import com.cobblemon.mod.common.net.messages.client.storage.pc.InitializePCPacket
import com.cobblemon.mod.common.util.alias
import com.cobblemon.mod.common.util.commandLang
import com.cobblemon.mod.common.util.party
import com.cobblemon.mod.common.util.pc
import com.cobblemon.mod.common.util.permission
import com.cobblemon.mod.common.util.player
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.server.level.ServerPlayer

object PokemonRestartCommand {

    private const val NAME = "pokemonrestart"
    private const val NAME_OTHER = "${NAME}other"
    private const val PLAYER = "player"
    private const val STARTERS = "reset_starters"
    private const val ALIAS = "pokerestart"
    private const val ALIAS_OTHER = "${ALIAS}other"

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val selfCommand = dispatcher.register(literal(NAME)
            .permission(CobblemonPermissions.POKEMON_EDIT_SELF)
            .then(argument(STARTERS, BoolArgumentType.bool())
                .executes { execute(it, it.source.playerOrException, BoolArgumentType.getBool(it, STARTERS)) }
            )
        )
        dispatcher.register(selfCommand.alias(ALIAS))

        val otherCommand = dispatcher.register(literal(NAME_OTHER)
            .permission(CobblemonPermissions.POKEMON_EDIT_OTHER)
            .then(argument(PLAYER, EntityArgument.player())
                .then(argument(STARTERS, BoolArgumentType.bool())
                    .executes { execute(it, it.player(), BoolArgumentType.getBool(it, STARTERS)) }
                )
            )
        )
        dispatcher.register(otherCommand.alias(ALIAS_OTHER))

        val selfCommandWithoutStarters = dispatcher.register(literal(NAME)
            .permission(CobblemonPermissions.POKEMON_EDIT_SELF)
                .executes { execute(it, it.source.playerOrException, false) }
        )
        dispatcher.register(selfCommandWithoutStarters.alias(ALIAS))

        val otherCommandWithoutStarters = dispatcher.register(literal(NAME_OTHER)
            .permission(CobblemonPermissions.POKEMON_EDIT_OTHER)
            .then(argument(PLAYER, EntityArgument.player())
                .executes { execute(it, it.player(), false) }
            )
        )
        dispatcher.register(otherCommandWithoutStarters.alias(ALIAS_OTHER))
    }

    private fun execute(context: CommandContext<CommandSourceStack>, player: ServerPlayer, resetStarters: Boolean): Int {
        resetPlayerPokemonData(player, resetStarters)
        context.source.sendSuccess({ commandLang(NAME, player.name) }, true)
        return Command.SINGLE_SUCCESS
    }

    private fun resetPlayerPokemonData(player: ServerPlayer, resetStarters: Boolean) {
        player.party().clearParty()
        player.pc().clearPC()
        player.sendPacket(InitializePartyPacket(true, player.uuid, player.party().size()))
        player.sendPacket(InitializePCPacket(player.uuid, player.pc().boxes.size, false))

        val playerData = Cobblemon.playerDataManager.getGenericData(player)
        playerData.starterPrompted = false
        playerData.starterLocked = false
        playerData.starterSelected = !resetStarters
        player.sendPacket(SetClientPlayerDataPacket(PlayerInstancedDataStoreTypes.GENERAL, playerData.toClientData()))
    }
}