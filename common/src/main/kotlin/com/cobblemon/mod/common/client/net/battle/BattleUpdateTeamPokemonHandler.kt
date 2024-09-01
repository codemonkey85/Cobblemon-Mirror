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
import com.cobblemon.mod.common.net.messages.client.battle.BattleUpdateTeamPokemonPacket
import net.minecraft.client.Minecraft

object BattleUpdateTeamPokemonHandler : ClientNetworkPacketHandler<BattleUpdateTeamPokemonPacket> {
    override fun handle(packet: BattleUpdateTeamPokemonPacket, client: Minecraft) {
        val battle = CobblemonClient.battle ?: return
        val actor = battle.side1.actors.find { it.uuid == Minecraft.getInstance().player?.uuid }
        if (actor != null) {
            val previous = actor.pokemon.find { it.uuid == packet.pokemon.uuid }
            if (previous !=  null) {
                actor.pokemon.add(actor.pokemon.indexOf(previous), packet.pokemon)
                actor.pokemon.remove(previous)
            }
        }
    }
}