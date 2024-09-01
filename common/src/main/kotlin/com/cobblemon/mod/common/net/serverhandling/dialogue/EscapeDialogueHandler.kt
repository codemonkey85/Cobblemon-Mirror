/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.dialogue

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.net.messages.server.dialogue.EscapeDialoguePacket
import com.cobblemon.mod.common.util.activeDialogue
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object EscapeDialogueHandler : ServerNetworkPacketHandler<EscapeDialoguePacket> {
    override fun handle(packet: EscapeDialoguePacket, server: MinecraftServer, player: ServerPlayer) {
        val dialogue = player.activeDialogue ?: return
        dialogue.escape()
    }
}