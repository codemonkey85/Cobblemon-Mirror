/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.storage.pc.link.PCLinkManager
import com.cobblemon.mod.common.api.storage.pc.link.PermissiblePcLink
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.net.messages.client.storage.pc.OpenPCPacket
import com.cobblemon.mod.common.util.*
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.literal

object PcCommand {

    private const val NAME = "pc"
    private val IN_BATTLE_EXCEPTION = SimpleCommandExceptionType(lang("pc.inbattle").red())

    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(literal(NAME)
            .permission(CobblemonPermissions.PC)
            .executes(this::execute)
        )
    }

    private fun execute(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        val pc = player.pc()
        if (player.isInBattle()) {
            throw IN_BATTLE_EXCEPTION.create()
        }
        PCLinkManager.addLink(PermissiblePcLink(pc, player, CobblemonPermissions.PC))
        OpenPCPacket(pc.uuid).sendToPlayer(player)
        context.source.level.playSoundServer(
                position = context.source.player!!.position(),
                sound = CobblemonSounds.PC_ON,
                volume = 0.5F,
                pitch = 1F
        )
        return Command.SINGLE_SUCCESS
    }

}
