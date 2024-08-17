/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.storage.pc

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.gui.pc.PCGUI
import com.cobblemon.mod.common.net.messages.client.storage.pc.ClosePCPacket
import net.minecraft.client.Minecraft

/**
 * Handles the request to close the PC GUI.
 *
 * @author Deltric
 * @since May 17th, 2023
 */
object ClosePCHandler : ClientNetworkPacketHandler<ClosePCPacket> {
    override fun handle(packet: ClosePCPacket, client: Minecraft) {
        if (client.screen !is PCGUI) {
            return
        }

        val pc = client.screen as PCGUI
        if (pc.pc.uuid != packet.storeID) {
            return
        }
        pc.configuration.exitFunction.invoke(pc)
    }
}