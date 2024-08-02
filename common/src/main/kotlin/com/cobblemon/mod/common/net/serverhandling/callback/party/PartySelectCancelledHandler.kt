/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.callback.party

import com.cobblemon.mod.common.api.callback.PartySelectCallbacks
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.net.messages.server.callback.party.PartySelectCancelledPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object PartySelectCancelledHandler : ServerNetworkPacketHandler<PartySelectCancelledPacket> {
    override fun handle(packet: PartySelectCancelledPacket, server: MinecraftServer, player: ServerPlayer) {
        PartySelectCallbacks.handleCancelled(player, packet.uuid)
    }
}