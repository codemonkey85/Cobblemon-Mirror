/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.gui

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.gui.summary.Summary
import com.cobblemon.mod.common.net.messages.client.ui.SummaryUIPacket
import net.minecraft.client.Minecraft

object SummaryUIPacketHandler: ClientNetworkPacketHandler<SummaryUIPacket> {
    override fun handle(packet: SummaryUIPacket, client: Minecraft) {
        try {
            Summary.open(
                party = packet.pokemon,
                editable = packet.editable
            )
        } catch (e: Exception) {
            Cobblemon.LOGGER.debug("Failed to open the summary from the SummaryUI packet handler", e)
        }
    }
}