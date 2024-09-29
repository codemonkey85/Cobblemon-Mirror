/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.pokedex.scanner

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.net.messages.server.pokedex.scanner.StartScanningPacket
import com.cobblemon.mod.common.pokedex.scanner.PlayerScanningDetails
import com.cobblemon.mod.common.pokedex.scanner.PokemonScanner
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object StartScanningHandler : ServerNetworkPacketHandler<StartScanningPacket> {
    override fun handle(
        packet: StartScanningPacket,
        server: MinecraftServer,
        player: ServerPlayer
    ) {
        val targetEntity = player.level().getEntity(packet.targetedId) ?: return
        if (PokemonScanner.isEntityInRange(player, targetEntity, packet.zoomLevel)) {
            PlayerScanningDetails.playerToEntityMap[player.uuid] = targetEntity.uuid
            PlayerScanningDetails.playerToTickMap[player.uuid] = server.tickCount
        }
    }
}