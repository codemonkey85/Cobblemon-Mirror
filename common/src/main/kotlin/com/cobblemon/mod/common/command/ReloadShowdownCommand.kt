/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.battles.runner.ShowdownService
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

object ReloadShowdownCommand {

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val command = Commands.literal("reloadshowdown")
            .requires { it.hasPermission(4) }
            .executes(::execute)
        dispatcher.register(command)
    }

    private fun execute(context: CommandContext<CommandSourceStack>): Int {
        try {
            ShowdownService.service.closeConnection()
            ShowdownService.service.openConnection()
            ShowdownService.service.registerSpecies()
            ShowdownService.service.registerBagItems()
            context.source.sendSystemMessage(Component.literal("Reloaded showdown"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Command.SINGLE_SUCCESS
    }

}