/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.pokemon.interact

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.server.pokemon.interact.InteractPokemonPacket
import com.cobblemon.mod.common.util.party
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object InteractPokemonHandler : ServerNetworkPacketHandler<InteractPokemonPacket> {
    override fun handle(packet: InteractPokemonPacket, server: MinecraftServer, player: ServerPlayer) {
        val pokemonEntity = player.serverLevel().getEntity(packet.pokemonID)
        if (pokemonEntity is PokemonEntity && !pokemonEntity.isBattleClone()) {
            if (packet.mountShoulder) {
                if (!pokemonEntity.canSitOnShoulder() || player.party().none { it == pokemonEntity.pokemon }) {
                    return
                }
                pokemonEntity.tryMountingShoulder(player)
            } else {
                pokemonEntity.offerHeldItem(player, player.mainHandItem)
            }
        }
    }
}