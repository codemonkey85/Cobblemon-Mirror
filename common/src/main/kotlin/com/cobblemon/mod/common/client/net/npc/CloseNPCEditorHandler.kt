/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.npc

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.gui.npc.NPCEditorScreen
import com.cobblemon.mod.common.net.messages.client.npc.CloseNPCEditorPacket
import net.minecraft.client.Minecraft

object CloseNPCEditorHandler : ClientNetworkPacketHandler<CloseNPCEditorPacket> {
    override fun handle(packet: CloseNPCEditorPacket, client: Minecraft) {
        val screen = client.screen
        if (screen is NPCEditorScreen) {
            screen.onClose()
        }
    }
}