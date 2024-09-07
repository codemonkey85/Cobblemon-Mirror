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
import com.cobblemon.mod.common.net.messages.client.battle.BattleCaptureShakePacket
import net.minecraft.client.Minecraft

object BattleCaptureShakeHandler : ClientNetworkPacketHandler<BattleCaptureShakePacket> {
    override fun handle(packet: BattleCaptureShakePacket, client: Minecraft) {
        val battle = CobblemonClient.battle ?: return
        val (_, activeBattlePokemon) = battle.getPokemonFromPNX(packet.targetPNX)
        val ballState = activeBattlePokemon.ballCapturing ?: return
        ballState.shakeEmitter.emit(Unit)
    }
}