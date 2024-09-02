/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.storage

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.net.messages.client.storage.RemoveClientPokemonPacket
import net.minecraft.client.Minecraft

object RemoveClientPokemonHandler : ClientNetworkPacketHandler<RemoveClientPokemonPacket> {
    override fun handle(packet: RemoveClientPokemonPacket, client: Minecraft) {
        if (packet.storeIsParty) {
            CobblemonClient.storage.removeFromParty(packet.storeID, packet.pokemonID)
        } else {
            CobblemonClient.storage.removeFromPC(packet.storeID, packet.pokemonID)
        }
    }
}