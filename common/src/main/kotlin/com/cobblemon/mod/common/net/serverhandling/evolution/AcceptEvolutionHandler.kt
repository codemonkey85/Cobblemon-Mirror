/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.evolution

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.net.messages.server.pokemon.update.evolution.AcceptEvolutionPacket
import com.cobblemon.mod.common.util.party
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object AcceptEvolutionHandler : ServerNetworkPacketHandler<AcceptEvolutionPacket> {
    override fun handle(packet: AcceptEvolutionPacket, server: MinecraftServer, player: ServerPlayer) {
        val pokemon = player.party()[packet.pokemonUUID] ?: return
        if (pokemon.entity?.isBusy == true) return
        val evolution = pokemon.evolutionProxy.server().firstOrNull { evolution -> evolution.id.equals(packet.evolutionId, true) } ?: return
        pokemon.evolutionProxy.server().start(evolution)
    }
}