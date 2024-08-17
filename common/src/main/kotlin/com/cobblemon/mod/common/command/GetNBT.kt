/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.util.requiresWithPermission
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.server.level.ServerPlayer

object GetNBT {
    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(Commands.literal("getnbt")
            .requiresWithPermission(CobblemonPermissions.GET_NBT) { it.player != null }
            .executes { execute(it, it.source.playerOrException) })
    }

    private fun execute(context: CommandContext<CommandSourceStack>, player: ServerPlayer) : Int {
        /*
        val stack = player.getItemInHand(Hand.MAIN_HAND)
        try {
            val formatter = NbtOrderedStringFormatter("", 0, mutableListOf())
            val str = formatter.apply(stack.nbt)
            player.sendMessage(str.text().suggest(str))
        } catch (e: Exception) {
            e.printStackTrace()
        }

         */
        return Command.SINGLE_SUCCESS
    }
}