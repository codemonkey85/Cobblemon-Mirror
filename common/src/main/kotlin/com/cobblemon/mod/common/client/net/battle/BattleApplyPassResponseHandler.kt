/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.battle

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.battles.ForcePassActionResponse
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.gui.battle.BattleGUI
import com.cobblemon.mod.common.net.messages.client.battle.BattleApplyPassResponsePacket
import net.minecraft.client.Minecraft

object BattleApplyPassResponseHandler : ClientNetworkPacketHandler<BattleApplyPassResponsePacket> {
    override fun handle(packet: BattleApplyPassResponsePacket, client: Minecraft) {
        val battle = CobblemonClient.battle ?: return
        val req = battle.pendingActionRequests.firstOrNull { it.response == null } ?: return
        val res = ForcePassActionResponse()
        val gui = Minecraft.getInstance().screen
        if (gui is BattleGUI) {
            gui.selectAction(req, res)
        } else {
            req.response = res
            battle.checkForFinishedChoosing()
        }
    }
}