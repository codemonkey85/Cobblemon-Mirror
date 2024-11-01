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
import com.cobblemon.mod.common.client.battle.ClientBattleChallenge
import com.cobblemon.mod.common.client.render.ClientPlayerIcon
import com.cobblemon.mod.common.net.messages.client.battle.BattleChallengeNotificationPacket
import net.minecraft.client.Minecraft

object BattleChallengeNotificationHandler : ClientNetworkPacketHandler<BattleChallengeNotificationPacket> {
    override fun handle(packet: BattleChallengeNotificationPacket, client: Minecraft) {
        val clientBattleChallenge = ClientBattleChallenge(packet.challengeID, packet.senderID, packet.expiryTime, packet.battleFormat)
        packet.challengerIDs.forEach {
            CobblemonClient.requests.battleChallenges[it] = clientBattleChallenge
            ClientPlayerIcon.update(it)
        }
    }
}