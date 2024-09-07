/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.dialogue.Dialogues
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.command.argument.DialogueArgumentType
import com.cobblemon.mod.common.util.alias
import com.cobblemon.mod.common.util.openDialogue
import com.cobblemon.mod.common.util.permission
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

object OpenDialogueCommand {
    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        val command = dispatcher.register(Commands.literal("opendialogue")
            .permission(CobblemonPermissions.OPEN_DIALOGUE)
            .then(
                Commands.argument("dialogue", DialogueArgumentType.dialogue())
                    .then(
                        Commands.argument("player", EntityArgument.player())
                            .executes {
                                val dialogueId = DialogueArgumentType.getDialogue(it, "dialogue")
                                if (!Dialogues.dialogues.containsKey(dialogueId)) {
                                    it.source.sendFailure("Invalid dialogue: $dialogueId".text())
                                    return@executes Command.SINGLE_SUCCESS
                                }
                                val player = EntityArgument.getPlayer(it, "player")
                                return@executes execute(it.source, dialogueId, player)
                            }
                    )
            )
        )
        dispatcher.register(command.alias("opendialogue"))
    }

    private fun execute(source: CommandSourceStack, dialogueId: ResourceLocation, player: ServerPlayer): Int {
        val dialogue = Dialogues.dialogues[dialogueId] ?: return run {
            source.sendSystemMessage("Invalid dialogue ID: $dialogueId".text())
            Command.SINGLE_SUCCESS
        }
        try {
            player.openDialogue(dialogue)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return Command.SINGLE_SUCCESS
    }
}