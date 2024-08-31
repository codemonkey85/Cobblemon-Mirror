/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.battle

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.net.messages.server.battle.RemoveSpectatorPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object RemoveSpectatorHandler : ServerNetworkPacketHandler<RemoveSpectatorPacket> {
    override fun handle(
        packet: RemoveSpectatorPacket,
        server: MinecraftServer,
        player: ServerPlayer
    ) {
        BattleRegistry.getBattle(packet.battleId)?.spectators?.remove(player.uuid)
    }

}