/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.pasture

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.gui.pc.PCGUI
import com.cobblemon.mod.common.net.messages.client.pasture.ClosePasturePacket
import net.minecraft.client.Minecraft

/**
 * Handles the request to close the pasture GUI.
 *
 * @author Deltric
 * @since May 17th, 2023
 */
object ClosePastureHandler: ClientNetworkPacketHandler<ClosePasturePacket> {

    override fun handle(packet: ClosePasturePacket, client: Minecraft) {
        if (client.screen !is PCGUI) {
            return
        }

        val pc = client.screen as PCGUI
        pc.configuration.exitFunction.invoke(pc)
    }

}