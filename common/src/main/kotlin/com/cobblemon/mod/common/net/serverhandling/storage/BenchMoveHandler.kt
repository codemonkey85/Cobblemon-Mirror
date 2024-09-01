/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.storage

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.api.storage.PokemonStore
import com.cobblemon.mod.common.api.storage.pc.link.PCLinkManager
import com.cobblemon.mod.common.net.messages.client.storage.pc.ClosePCPacket
import com.cobblemon.mod.common.net.messages.server.BenchMovePacket
import com.cobblemon.mod.common.util.party
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object BenchMoveHandler : ServerNetworkPacketHandler<BenchMovePacket> {
    override fun handle(packet: BenchMovePacket, server: MinecraftServer, player: ServerPlayer) {
        val pokemonStore: PokemonStore<*> = if (packet.isParty) {
            player.party()
        } else {
            PCLinkManager.getPC(player) ?: return run { ClosePCPacket(null).sendToPlayer(player) }
        }

        val pokemon = pokemonStore[packet.uuid] ?: return

        if (pokemon.moveSet.getMovesWithNulls().none { it?.template == packet.oldMove } || pokemon.moveSet.any { it.template == packet.newMove }) {
            // Something inconsistent in the information they're sending, better give them an update on their moveset
            // in case they're just out of date somehow.
            pokemon.moveSet.update()
            return
        }

        if (packet.newMove != null && packet.newMove !in pokemon.allAccessibleMoves) {
            LOGGER.warn("${player.name} tried to bench ${packet.oldMove?.name} for ${packet.newMove.name} but it doesn't have ${packet.newMove.name} learned. Could be a hacker!")
            return
        }

        pokemon.exchangeMove(packet.oldMove, packet.newMove)
    }
}