/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.callback.move

import com.cobblemon.mod.common.api.callback.MoveSelectCallbacks
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.net.messages.server.callback.move.MoveSelectedPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object MoveSelectedHandler : ServerNetworkPacketHandler<MoveSelectedPacket> {
    override fun handle(packet: MoveSelectedPacket, server: MinecraftServer, player: ServerPlayer) {
        MoveSelectCallbacks.handleCallback(player, packet.uuid, packet.index)
    }
}