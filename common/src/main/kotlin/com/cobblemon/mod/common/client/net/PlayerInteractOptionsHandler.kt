/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.gui.interact.wheel.createPlayerInteractGui
import com.cobblemon.mod.common.net.messages.client.PlayerInteractOptionsPacket
import net.minecraft.client.Minecraft

object PlayerInteractOptionsHandler : ClientNetworkPacketHandler<PlayerInteractOptionsPacket> {
    override fun handle(packet: PlayerInteractOptionsPacket, client: Minecraft) {
        Minecraft.getInstance().setScreen(createPlayerInteractGui(packet))
    }

}