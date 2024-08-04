/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.battle

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.battle.ClientBallDisplay
import com.cobblemon.mod.common.net.messages.client.battle.BattleCaptureStartPacket
import net.minecraft.client.Minecraft

object BattleCaptureStartHandler : ClientNetworkPacketHandler<BattleCaptureStartPacket> {
    override fun handle(packet: BattleCaptureStartPacket, client: Minecraft) {
        val battle = CobblemonClient.battle ?: return
        val targetPokemon = battle.getPokemonFromPNX(packet.targetPNX)
        val tile = targetPokemon.second
        val pokeBall = PokeBalls.getPokeBall(packet.pokeBallType) ?: PokeBalls.POKE_BALL
        tile.ballCapturing = ClientBallDisplay(pokeBall, packet.aspects).also { it.start() }
    }
}