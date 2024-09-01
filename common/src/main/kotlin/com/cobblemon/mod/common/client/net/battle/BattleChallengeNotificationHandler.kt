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
import com.cobblemon.mod.common.client.keybind.boundKey
import com.cobblemon.mod.common.client.keybind.keybinds.PartySendBinding
import com.cobblemon.mod.common.net.messages.client.battle.BattleChallengeNotificationPacket
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.Minecraft

object BattleChallengeNotificationHandler : ClientNetworkPacketHandler<BattleChallengeNotificationPacket> {
    override fun handle(packet: BattleChallengeNotificationPacket, client: Minecraft) {
        CobblemonClient.requests.battleChallenges.add(ClientBattleChallenge(packet.battleChallengeId, packet.challengerId))
        client.player?.displayClientMessage(
            lang(
                "challenge.receiver",
                packet.challengerName,
                PartySendBinding.boundKey().displayName
            ),
            true
        )
    }
}