/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.callback.partymove

import com.cobblemon.mod.common.api.callback.PartyMoveSelectCallbacks
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.net.messages.server.callback.partymove.PartyPokemonMoveSelectedPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object PartyPokemonMoveSelectedHandler : ServerNetworkPacketHandler<PartyPokemonMoveSelectedPacket> {
    override fun handle(packet: PartyPokemonMoveSelectedPacket, server: MinecraftServer, player: ServerPlayer) {
        PartyMoveSelectCallbacks.handleCallback(player, packet.uuid, packet.pokemonIndex, packet.moveIndex)
    }
}