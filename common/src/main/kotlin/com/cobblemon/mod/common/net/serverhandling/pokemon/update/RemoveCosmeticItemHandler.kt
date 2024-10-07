/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.pokemon.update

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.api.storage.PokemonStore
import com.cobblemon.mod.common.api.storage.pc.link.PCLinkManager
import com.cobblemon.mod.common.net.messages.client.storage.pc.ClosePCPacket
import com.cobblemon.mod.common.net.messages.server.pokemon.update.RemoveCosmeticItemPacket
import com.cobblemon.mod.common.util.giveOrDropItemStack
import com.cobblemon.mod.common.util.party
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object RemoveCosmeticItemHandler : ServerNetworkPacketHandler<RemoveCosmeticItemPacket> {
    override fun handle(packet: RemoveCosmeticItemPacket, server: MinecraftServer, player: ServerPlayer) {
        val pokemonStore: PokemonStore<*> = if (packet.isParty) {
            player.party()
        } else {
            PCLinkManager.getPC(player) ?: return run { ClosePCPacket(null).sendToPlayer(player) }
        }

        val pokemon = pokemonStore[packet.pokemonId] ?: return

        if (!pokemon.cosmeticItem.isEmpty) {
            player.giveOrDropItemStack(pokemon.cosmeticItem)
        }
    }
}