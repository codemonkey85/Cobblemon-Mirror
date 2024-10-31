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
import com.cobblemon.mod.common.client.battle.animations.MoveTileOffscreenAnimation
import com.cobblemon.mod.common.net.messages.client.battle.BattleCaptureEndPacket
import net.minecraft.client.Minecraft

object BattleCaptureEndHandler : ClientNetworkPacketHandler<BattleCaptureEndPacket> {
    override fun handle(packet: BattleCaptureEndPacket, client: Minecraft) {
        val battle = CobblemonClient.battle ?: return
        val overlay = CobblemonClient.battleOverlay
        val (_, activeBattlePokemon) = battle.getPokemonFromPNX(packet.targetPNX)
        if (packet.succeeded) {
            activeBattlePokemon.animations.add(MoveTileOffscreenAnimation().also { overlay.after(seconds = it.duration) { activeBattlePokemon.ballCapturing = null } })
        }
        activeBattlePokemon.ballCapturing = null
    }
}