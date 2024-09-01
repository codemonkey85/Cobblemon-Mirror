/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.storage.pc

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.api.pasture.PastureLinkManager
import com.cobblemon.mod.common.api.storage.pc.link.PCLinkManager
import com.cobblemon.mod.common.net.messages.server.storage.pc.UnlinkPlayerFromPCPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object UnlinkPlayerFromPCHandler : ServerNetworkPacketHandler<UnlinkPlayerFromPCPacket> {
    override fun handle(packet: UnlinkPlayerFromPCPacket, server: MinecraftServer, player: ServerPlayer) {
        PCLinkManager.removeLink(player.uuid)
        PastureLinkManager.links.remove(player.uuid) // Can remove this if same logic is done in closing a pasture GUI
    }
}