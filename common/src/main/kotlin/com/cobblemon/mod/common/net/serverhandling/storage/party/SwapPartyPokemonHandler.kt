/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.storage.party

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.net.messages.server.storage.party.SwapPartyPokemonPacket
import com.cobblemon.mod.common.util.party
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object SwapPartyPokemonHandler : ServerNetworkPacketHandler<SwapPartyPokemonPacket> {
    override fun handle(packet: SwapPartyPokemonPacket, server: MinecraftServer, player: ServerPlayer) {
        val party = player.party()
        val pokemon1 = party[packet.position1] ?: return
        val pokemon2 = party[packet.position2] ?: return

        if (pokemon1.uuid != packet.pokemon1ID || pokemon2.uuid != packet.pokemon2ID || packet.position1 == packet.position2) {
            return
        }
        party.swap(packet.position1, packet.position2)
    }
}