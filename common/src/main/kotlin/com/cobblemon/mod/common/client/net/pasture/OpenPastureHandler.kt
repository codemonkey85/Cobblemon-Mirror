/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.pasture

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.api.reactive.SettableObservable
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.gui.pasture.PasturePCGUIConfiguration
import com.cobblemon.mod.common.client.gui.pc.PCGUI
import com.cobblemon.mod.common.net.messages.client.pasture.OpenPasturePacket
import net.minecraft.client.Minecraft

object OpenPastureHandler : ClientNetworkPacketHandler<OpenPasturePacket> {
    override fun handle(packet: OpenPasturePacket, client: Minecraft) {

        val pcConfiguration = PasturePCGUIConfiguration(
            pastureId = packet.pastureId,
            limit = packet.limit,
            permissions = packet.permissions,
            pasturedPokemon = SettableObservable(packet.tetheredPokemon)
        )

        client.setScreen(PCGUI(pc = CobblemonClient.storage.pcStores[packet.pcId]!!, party = CobblemonClient.storage.myParty, configuration = pcConfiguration))
    }
}