/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.battle

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.api.snowstorm.BedrockParticleOptions
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.battle.ClientBattleChallenge
import com.cobblemon.mod.common.client.particle.BedrockParticleOptionsRepository
import com.cobblemon.mod.common.client.particle.ParticleStorm
import com.cobblemon.mod.common.client.render.MatrixWrapper
import com.cobblemon.mod.common.net.messages.client.battle.BattleChallengeNotificationPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.getPlayer
import com.cobblemon.mod.common.util.lang
import com.mojang.authlib.minecraft.client.MinecraftClient
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel

object BattleChallengeNotificationHandler : ClientNetworkPacketHandler<BattleChallengeNotificationPacket> {
    override fun handle(packet: BattleChallengeNotificationPacket, client: Minecraft) {
        val clientBattleChallenge = ClientBattleChallenge(packet.battleChallengeId, packet.challengerIds, packet.battleFormat)
        CobblemonClient.requests.battleChallenges.add(clientBattleChallenge)
        client.player?.sendSystemMessage(
            lang(
                "challenge.receiver",
                packet.challengerNames.first(),
                lang("battle.types.${packet.battleFormat.battleType.name}"),
            ).yellow()
        )
        CobblemonClient.requests.addParticleEffect(clientBattleChallenge)
    }
}