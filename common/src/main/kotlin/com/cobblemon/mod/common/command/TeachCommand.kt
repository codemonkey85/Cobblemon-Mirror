/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.moves.BenchedMove
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.pokemon.moves.LearnsetQuery
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.command.argument.MoveArgumentType
import com.cobblemon.mod.common.command.argument.PartySlotArgumentType
import com.cobblemon.mod.common.util.commandLang
import com.cobblemon.mod.common.util.permission
import com.cobblemon.mod.common.util.player
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.server.level.ServerPlayer

object TeachCommand {

    private const val NAME = "teach"
    private const val PLAYER = "player"
    private const val SLOT = "slot"
    private const val MOVE = "move"
    private val ALREADY_KNOWS_EXCEPTION = Dynamic2CommandExceptionType { a, b -> commandLang("$NAME.already_knows", a, b).red() }
    private val CANT_LEARN_EXCEPTION = Dynamic2CommandExceptionType { a, b -> commandLang("$NAME.cant_learn", a, b).red() }

    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        val command = Commands.literal(NAME)
            .permission(CobblemonPermissions.TEACH)
            .then(Commands.argument(PLAYER, EntityArgument.player())
                .then(Commands.argument(SLOT, PartySlotArgumentType.partySlot())
                    .then(Commands.argument(MOVE, MoveArgumentType.move())
                        .executes { execute(it, it.player()) }
                    ))
            )
        dispatcher.register(command)
    }

    private fun execute(context: CommandContext<CommandSourceStack>, player: ServerPlayer) : Int {
        val pokemon = PartySlotArgumentType.getPokemonOf(context, SLOT, player)
        val move = MoveArgumentType.getMove(context, MOVE)

        if (!Cobblemon.permissionValidator.hasPermission(context.source, CobblemonPermissions.TEACH_BYPASS_LEARNSET) && !LearnsetQuery.ANY.canLearn(move, pokemon.form.moves)) {
            throw CANT_LEARN_EXCEPTION.create(pokemon.getDisplayName(), move.displayName)
        }

        if (pokemon.moveSet.getMoves().any { it.template == move } || pokemon.benchedMoves.any { it.moveTemplate == move }) {
            throw ALREADY_KNOWS_EXCEPTION.create(pokemon.getDisplayName(), move.displayName)
        }

        if (pokemon.moveSet.hasSpace()) {
            pokemon.moveSet.add(move.create())
        }
        else {
            pokemon.benchedMoves.add(BenchedMove(move, 0))
        }

        val pokemonLearntMessage = commandLang(NAME, pokemon.species.translatedName, player.name, move.displayName)
        context.source.sendSuccess({ pokemonLearntMessage }, true)

        if (context.source.player?.equals(player) != true) {
            player.sendSystemMessage(pokemonLearntMessage)
        }

        return Command.SINGLE_SUCCESS
    }

}