/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreType
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreTypes
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.net.messages.client.starter.OpenStarterUIPacket
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.permission
import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.EntityArgument

object OpenStarterScreenCommand {

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            literal("openstarterscreen")
                .permission(CobblemonPermissions.OPEN_STARTER_SCREEN)
                .then(
                    argument("player", EntityArgument.player())
                        .executes { execute(it) }
                )
        )
    }

    private fun execute(context: CommandContext<CommandSourceStack>) : Int {
        val player = EntityArgument.getPlayer(context, "player")
        val playerData = Cobblemon.playerDataManager.getGenericData(player)
        if (playerData.starterSelected) {
            context.source.sendSuccess({ lang("ui.starter.hasalreadychosen", player.name).red() }, true)
            return 0
        }
        if (playerData.starterLocked) {
            playerData.starterLocked = false
            playerData.sendToPlayer(player)
        }
        playerData.starterPrompted = true
        Cobblemon.playerDataManager.saveSingle(playerData, PlayerInstancedDataStoreTypes.GENERAL)
        player.sendPacket(OpenStarterUIPacket(Cobblemon.starterHandler.getStarterList(player)))
        return SINGLE_SUCCESS
    }

}