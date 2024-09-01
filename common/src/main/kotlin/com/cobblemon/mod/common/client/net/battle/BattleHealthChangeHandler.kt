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
import com.cobblemon.mod.common.client.battle.animations.HealthChangeAnimation
import com.cobblemon.mod.common.net.messages.client.battle.BattleHealthChangePacket
import net.minecraft.client.Minecraft

object BattleHealthChangeHandler : ClientNetworkPacketHandler<BattleHealthChangePacket> {
    override fun handle(packet: BattleHealthChangePacket, client: Minecraft) {
        val battle = CobblemonClient.battle ?: return
        val (_, activePokemon) = battle.getPokemonFromPNX(packet.pnx)
        packet.newMaxHealth?.let { activePokemon.battlePokemon?.maxHp = it }
        activePokemon.animations.add(
            HealthChangeAnimation(newHealth = packet.newHealth)
        )
    }
}