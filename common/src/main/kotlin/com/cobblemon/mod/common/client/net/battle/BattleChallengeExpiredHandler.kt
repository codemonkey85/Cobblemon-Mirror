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
import com.cobblemon.mod.common.net.messages.client.battle.BattleChallengeExpiredPacket
import net.minecraft.client.Minecraft

object BattleChallengeExpiredHandler : ClientNetworkPacketHandler<BattleChallengeExpiredPacket> {
    override fun handle(packet: BattleChallengeExpiredPacket, client: Minecraft) {
        CobblemonClient.requests.battleChallenges.removeIf { it.challengeId == packet.battleChallengeId }
    }
}