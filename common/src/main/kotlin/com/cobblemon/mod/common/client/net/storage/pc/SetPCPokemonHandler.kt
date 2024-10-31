/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.storage.pc

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.net.messages.client.storage.pc.SetPCPokemonPacket
import net.minecraft.client.Minecraft

object SetPCPokemonHandler : ClientNetworkPacketHandler<SetPCPokemonPacket> {
    override fun handle(packet: SetPCPokemonPacket, client: Minecraft) {
        CobblemonClient.storage.setPCPokemon(packet.storeID, packet.storePosition, packet.pokemon(client.level!!.registryAccess()))
    }
}