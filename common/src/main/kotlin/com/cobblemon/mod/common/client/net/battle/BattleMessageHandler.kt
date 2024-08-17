/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.battle

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.font
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.battle.widgets.BattleMessagePane
import com.cobblemon.mod.common.net.messages.client.battle.BattleMessagePacket
import net.minecraft.client.Minecraft
import net.minecraft.locale.Language

object BattleMessageHandler : ClientNetworkPacketHandler<BattleMessagePacket> {
    override fun handle(packet: BattleMessagePacket, client: Minecraft) {
        val battle = CobblemonClient.battle ?: return
        val textRenderer = Minecraft.getInstance().font
        for (message in packet.messages) {
            val line = message.copy().bold().font(CobblemonResources.DEFAULT_LARGE)
            val lines = Language.getInstance().getVisualOrder(textRenderer.splitter.splitLines(line, BattleMessagePane.LINE_WIDTH, line.style))
            battle.messages.add(lines)
        }
    }
}