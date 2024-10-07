/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.pokemon.update

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonCosmeticItems
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.api.storage.PokemonStore
import com.cobblemon.mod.common.api.storage.pc.link.PCLinkManager
import com.cobblemon.mod.common.net.messages.client.storage.pc.ClosePCPacket
import com.cobblemon.mod.common.net.messages.server.pokemon.update.ApplyCosmeticItemPacket
import com.cobblemon.mod.common.util.giveOrDropItemStack
import com.cobblemon.mod.common.util.itemRegistry
import com.cobblemon.mod.common.util.party
import com.cobblemon.mod.common.util.usableItems
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object ApplyCosmeticItemHandler : ServerNetworkPacketHandler<ApplyCosmeticItemPacket> {
    override fun handle(packet: ApplyCosmeticItemPacket, server: MinecraftServer, player: ServerPlayer) {
        val cosmeticItem = CobblemonCosmeticItems.cosmeticItems.find { it.id == packet.id }?.cosmeticItems?.getOrNull(packet.cosmeticItemIndex)
            ?: return Cobblemon.LOGGER.error("${player.name} attempted to apply a cosmetic item that does not exist, ${packet.id} index ${packet.cosmeticItemIndex}. Cobblemon defect or client hacking? Vote now on your phones.")

        val pokemonStore: PokemonStore<*> = if (packet.isParty) {
            player.party()
        } else {
            PCLinkManager.getPC(player) ?: return run { ClosePCPacket(null).sendToPlayer(player) }
        }

        val pokemon = pokemonStore[packet.pokemonId] ?: return
        val itemStack = cosmeticItem.findMatchingItemStack(player)
            ?: return Cobblemon.LOGGER.error("${player.name} attempted to apply a cosmetic item that requires an item they do not have. Desync? A hack? .... A defect? Report this!")

        val consumedItem = itemStack.split(1)

        if (!pokemon.cosmeticItem.isEmpty) {
            player.giveOrDropItemStack(pokemon.cosmeticItem)
        }

        pokemon.cosmeticItem = consumedItem
    }
}