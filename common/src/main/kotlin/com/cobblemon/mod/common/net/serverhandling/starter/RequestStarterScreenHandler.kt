/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.starter

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.net.messages.server.starter.RequestStarterScreenPacket
import com.cobblemon.mod.common.util.lang
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object RequestStarterScreenHandler : ServerNetworkPacketHandler<RequestStarterScreenPacket> {
    override fun handle(packet: RequestStarterScreenPacket, server: MinecraftServer, player: ServerPlayer) {
        val playerData = Cobblemon.playerDataManager.getGenericData(player)
        if (playerData.starterSelected) {
            return player.sendSystemMessage(lang("ui.starter.alreadyselected").red())
        } else if (playerData.starterLocked) {
            return player.sendSystemMessage(lang("ui.starter.cannotchoose").red())
        } else {
            Cobblemon.starterHandler.requestStarterChoice(player)
        }
    }
}