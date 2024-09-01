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
import com.cobblemon.mod.common.util.requiresWithPermission
import com.cobblemon.mod.common.util.traceFirstEntityCollision
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.server.level.ServerPlayer

object NPCEditCommand {
    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("npcedit")
            .requiresWithPermission(CobblemonPermissions.NPC_EDIT) { it.player != null }
            .executes { execute(it, it.source.playerOrException) })
    }

    private fun execute(context: CommandContext<CommandSourceStack>, player: ServerPlayer) : Int {
        val targetEntity = player.traceFirstEntityCollision(entityClass = NPCEntity::class.java)
        if (targetEntity == null) {
            player.sendSystemMessage(commandLang("npcedit.non_npc").red())
            return 0
        }

        targetEntity.edit(player)
        return Command.SINGLE_SUCCESS
    }
}