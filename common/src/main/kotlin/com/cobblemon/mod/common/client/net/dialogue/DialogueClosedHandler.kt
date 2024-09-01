/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.dialogue

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.gui.dialogue.DialogueScreen
import com.cobblemon.mod.common.net.messages.client.dialogue.DialogueClosedPacket
import net.minecraft.client.Minecraft

object DialogueClosedHandler : ClientNetworkPacketHandler<DialogueClosedPacket> {
    override fun handle(packet: DialogueClosedPacket, client: Minecraft) {
        val currentScreen = client.screen as? DialogueScreen ?: return
        if (packet.dialogueId == null || currentScreen.dialogueId == packet.dialogueId) {
            client.setScreen(null)
        }
    }
}