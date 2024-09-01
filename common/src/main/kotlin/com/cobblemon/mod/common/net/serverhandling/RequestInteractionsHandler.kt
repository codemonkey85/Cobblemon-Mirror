/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.net.messages.client.PlayerInteractOptionsPacket
import com.cobblemon.mod.common.net.messages.server.RequestPlayerInteractionsPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import java.util.EnumSet

object RequestInteractionsHandler : ServerNetworkPacketHandler<RequestPlayerInteractionsPacket> {
    override fun handle(
        packet: RequestPlayerInteractionsPacket,
        server: MinecraftServer,
        player: ServerPlayer
    ) {
        //We could potentially check if the targeted player has pokemon here
        val options = EnumSet.of(PlayerInteractOptionsPacket.Options.TRADE)

        val isTargetBattling = BattleRegistry.getBattleByParticipatingPlayerId(packet.targetId) != null
        if (isTargetBattling and Cobblemon.config.allowSpectating) {
            options.add(PlayerInteractOptionsPacket.Options.SPECTATE_BATTLE)
        }
        else {
            options.add(PlayerInteractOptionsPacket.Options.BATTLE)
        }
        PlayerInteractOptionsPacket(options, packet.targetId, packet.targetNumericId, packet.pokemonId).sendToPlayer(player)

    }

}