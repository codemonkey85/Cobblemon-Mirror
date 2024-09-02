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
import com.cobblemon.mod.common.command.argument.PokemonPropertiesArgumentType
import com.cobblemon.mod.common.util.alias
import com.cobblemon.mod.common.util.commandLang
import com.cobblemon.mod.common.util.permission
import com.cobblemon.mod.common.util.player
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.server.level.ServerPlayer

object PokemonEditCommand {

    private const val NAME = "pokemonedit"
    private const val NAME_OTHER = "${NAME}other"
    private const val PLAYER = "player"
    private const val SLOT = "slot"
    private const val PROPERTIES = "properties"
    private const val ALIAS = "pokeedit"
    private const val ALIAS_OTHER = "${ALIAS}other"

    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        val selfCommand = dispatcher.register(literal(NAME)
            .permission(CobblemonPermissions.POKEMON_EDIT_SELF)
            .then(argument(SLOT, PartySlotArgumentType.partySlot())
                .then(argument(PROPERTIES, PokemonPropertiesArgumentType.properties())
                    .executes{ execute(it, it.source.playerOrException) }
                )
            )
        )
        dispatcher.register(selfCommand.alias(ALIAS))

        val otherCommand = dispatcher.register(literal(NAME_OTHER)
            .permission(CobblemonPermissions.POKEMON_EDIT_OTHER)
            .then(argument(PLAYER, EntityArgument.player())
                .then(argument(SLOT, PartySlotArgumentType.partySlot())
                    .then(argument(PROPERTIES, PokemonPropertiesArgumentType.properties())
                        .executes { execute(it, it.player()) }
                    )
                )
            )
        )
        dispatcher.register(otherCommand.alias(ALIAS_OTHER))
    }

    private fun execute(context: CommandContext<CommandSourceStack>, player: ServerPlayer): Int {
        val pokemon = PartySlotArgumentType.getPokemonOf(context, SLOT, player)
        val oldName = pokemon.species.translatedName
        val properties = PokemonPropertiesArgumentType.getPokemonProperties(context, PROPERTIES)
        properties.apply(pokemon)
        context.source.sendSuccess({ commandLang(NAME, oldName, player.name) }, true)
        return Command.SINGLE_SUCCESS
    }

}