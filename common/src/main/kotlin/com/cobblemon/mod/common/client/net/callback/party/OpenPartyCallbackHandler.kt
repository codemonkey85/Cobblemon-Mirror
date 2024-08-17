/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.callback.party

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.gui.interact.partyselect.PartySelectGUI
import com.cobblemon.mod.common.net.messages.client.callback.OpenPartyCallbackPacket
import net.minecraft.client.Minecraft

object OpenPartyCallbackHandler : ClientNetworkPacketHandler<OpenPartyCallbackPacket> {
    override fun handle(packet: OpenPartyCallbackPacket, client: Minecraft) {
        client.setScreen(
            PartySelectGUI(
                title = packet.title,
                pokemon = packet.pokemon,
//                usePortraits = packet.usePortraits,
//                animate = packet.animate,
                uuid = packet.uuid
            )
        )
    }
}