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
import com.cobblemon.mod.common.net.messages.client.storage.SwapClientPokemonPacket
import net.minecraft.client.Minecraft

object SwapClientPokemonHandler : ClientNetworkPacketHandler<SwapClientPokemonPacket> {
    override fun handle(packet: SwapClientPokemonPacket, client: Minecraft) {
        if (packet.storeIsParty) {
            CobblemonClient.storage.swapInParty(packet.storeID, packet.pokemonID1, packet.pokemonID2)
        } else {
            CobblemonClient.storage.swapInPC(packet.storeID, packet.pokemonID1, packet.pokemonID2)
        }
    }
}