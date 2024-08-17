/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.util.*
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.server.level.ServerPlayer

object HealPokemonCommand {

    private val IN_BATTLE_EXCEPTION = SimpleCommandExceptionType(commandLang("pokeheal.in_battle").red())

    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        val command = dispatcher.register(literal("healpokemon")
            .permission(CobblemonPermissions.HEAL_POKEMON_SELF)
            .executes { execute(it.source, it.source.playerOrException) }
            .then(
                Commands.argument("player", EntityArgument.player())
                    .permission(CobblemonPermissions.HEAL_POKEMON_OTHER)
                    .executes { execute(it.source, it.player("player")) }
            ))
        dispatcher.register(command.alias("pokeheal"))
    }

    private fun execute(source: CommandSourceStack, target: ServerPlayer) : Int {
        if (target.isInBattle()) {
            throw IN_BATTLE_EXCEPTION.create()
        }
        if (!target.level().isClientSide) {
            val party = target.party()
            party.heal()
            source.sendSuccess({ commandLang("healpokemon.heal", target.name) }, true)
        }
        return Command.SINGLE_SUCCESS
    }

}