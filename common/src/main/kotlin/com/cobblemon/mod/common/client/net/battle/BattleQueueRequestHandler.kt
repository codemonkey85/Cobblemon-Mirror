/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.battle

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.battle.SingleActionRequest
import com.cobblemon.mod.common.net.messages.client.battle.BattleQueueRequestPacket
import net.minecraft.client.Minecraft

object BattleQueueRequestHandler : ClientNetworkPacketHandler<BattleQueueRequestPacket> {
    override fun handle(packet: BattleQueueRequestPacket, client: Minecraft) {
        val battle = CobblemonClient.battle ?: return
        val actor = battle.side1.actors.find { it.uuid == Minecraft.getInstance().player?.uuid } ?: return
        CobblemonClient.battle?.pendingActionRequests = SingleActionRequest.composeFrom(actor, packet.request)
    }
}