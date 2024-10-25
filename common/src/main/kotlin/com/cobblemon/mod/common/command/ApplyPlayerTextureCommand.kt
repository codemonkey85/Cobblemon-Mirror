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
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.util.commandLang
import com.cobblemon.mod.common.util.permission
import com.cobblemon.mod.common.util.traceFirstEntityCollision
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object ApplyPlayerTextureCommand {
    private const val NAME = "applyplayertexture"
    private const val PLAYER = "player"

    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        val command = Commands.literal(NAME)
            .permission(CobblemonPermissions.APPLY_PLAYER_TEXTURE)
            .then(Commands.argument(PLAYER, StringArgumentType.word()).executes(::execute))
        dispatcher.register(command)
    }

    private fun execute(context: CommandContext<CommandSourceStack>) : Int {
        val player = context.source.playerOrException
        val target = StringArgumentType.getString(context, "player")
        val targetEntity = player.traceFirstEntityCollision(entityClass = NPCEntity::class.java)
        if (targetEntity == null) {
            player.sendSystemMessage(commandLang("npcedit.non_npc").red())
            return 0
        }

        targetEntity.loadTextureFromGameProfileName(target)
        return Command.SINGLE_SUCCESS
    }
}