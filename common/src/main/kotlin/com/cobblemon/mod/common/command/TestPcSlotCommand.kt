/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.pokemon.PokemonPropertyExtractor
import com.cobblemon.mod.common.api.storage.pc.POKEMON_PER_BOX
import com.cobblemon.mod.common.command.argument.PokemonPropertiesArgumentType
import com.cobblemon.mod.common.util.pc
import com.cobblemon.mod.common.util.permission
import com.cobblemon.mod.common.util.player
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.EntityArgument

object TestPcSlotCommand {

    private const val NAME = "testpcslot"
    private const val PLAYER = "player"
    private const val BOX = "box"
    private const val SLOT = "slot"
    private const val PROPERTIES = "properties"
    private const val NO_SUCCESS = 0

    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            literal(NAME)
            .permission(CobblemonPermissions.TEST_PC_SLOT)
            .then(argument(PLAYER, EntityArgument.player())
            .then(argument(BOX, IntegerArgumentType.integer(1, Cobblemon.config.defaultBoxCount))
            .then(argument(SLOT, IntegerArgumentType.integer(1, POKEMON_PER_BOX))
            .then(argument(PROPERTIES, PokemonPropertiesArgumentType.properties())
            .executes(this::execute)))))
        )
    }

    private fun execute(context: CommandContext<CommandSourceStack>): Int {
        val player = context.player(PLAYER)
        val boxNumber = IntegerArgumentType.getInteger(context, BOX)
        val slot = IntegerArgumentType.getInteger(context, SLOT)
        val properties = PokemonPropertiesArgumentType.getPokemonProperties(context, PROPERTIES)
        return if (player.pc().boxes[boxNumber - 1][slot - 1]?.createPokemonProperties(PokemonPropertyExtractor.ALL)
                ?.let { properties.isSubSetOf(it) } == true) Command.SINGLE_SUCCESS else NO_SUCCESS
    }
}