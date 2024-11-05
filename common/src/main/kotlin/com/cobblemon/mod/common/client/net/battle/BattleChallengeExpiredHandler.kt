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
import com.cobblemon.mod.common.client.render.ClientPlayerIcon
import com.cobblemon.mod.common.net.messages.client.battle.BattleChallengeExpiredPacket
import net.minecraft.client.Minecraft

object BattleChallengeExpiredHandler : ClientNetworkPacketHandler<BattleChallengeExpiredPacket> {
    override fun handle(packet: BattleChallengeExpiredPacket, client: Minecraft) {
        val iter = CobblemonClient.requests.battleChallenges.iterator()
        while(iter.hasNext()) {
            val entry = iter.next()
            val player = entry.key
            if (entry.value.requestID == packet.requestID) {
                iter.remove()
                ClientPlayerIcon.update(player)
            }
        }
    }
}