/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.pokemon.experience.CommandExperienceSource
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.util.party
import com.cobblemon.mod.common.util.permission
import com.cobblemon.mod.common.util.player
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.server.level.ServerPlayer

object LevelUp {

    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        val command = Commands.literal("levelup")
            .permission(CobblemonPermissions.LEVEL_UP_SELF)
            .then(
                Commands.argument("player", EntityArgument.player())
                    .permission(CobblemonPermissions.LEVEL_UP_OTHER)
                    .then(
                        Commands.argument("slot", IntegerArgumentType.integer(1, 99))
                            .executes { execute(it, it.player()) }
                    )
            )
            .then(
                Commands.argument("slot", IntegerArgumentType.integer(1, 99))
                    .requires { it.entity is ServerPlayer && it.player != null }
                    .executes { execute(it, it.source.playerOrException) }
            )

        dispatcher.register(command)
    }

    private fun execute(context: CommandContext<CommandSourceStack>, player: ServerPlayer) : Int {
        val slot = IntegerArgumentType.getInteger(context, "slot")
        val party = player.party()
        if (slot > party.size()) {
            // todo translate
            context.source.sendFailure("Your party only has ${party.size()} slots.".text())
            return 0
        }

        val pokemon = party.get(slot - 1)
        if (pokemon == null) {
            context.source.sendFailure("There is no Pokémon in slot $slot".text())
            return 0
        }

        val source = CommandExperienceSource(context.source)
        pokemon.addExperienceWithPlayer(player, source, pokemon.getExperienceToNextLevel())
        return Command.SINGLE_SUCCESS
    }
}