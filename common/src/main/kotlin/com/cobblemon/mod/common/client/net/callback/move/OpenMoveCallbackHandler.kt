/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.callback.move

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.gui.interact.moveselect.MoveSelectGUI
import com.cobblemon.mod.common.net.messages.client.callback.OpenMoveCallbackPacket
import net.minecraft.client.Minecraft

object OpenMoveCallbackHandler : ClientNetworkPacketHandler<OpenMoveCallbackPacket> {
    override fun handle(packet: OpenMoveCallbackPacket, client: Minecraft) {
        client.setScreen(MoveSelectGUI(packet.title, packet.moves, packet.uuid))
    }
}