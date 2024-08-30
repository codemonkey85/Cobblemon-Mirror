/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.pokedex.scanner

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokedex.scanning.PokemonScannedEvent
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.pokedex.ServerConfirmedScanPacket
import com.cobblemon.mod.common.net.messages.server.pokedex.scanner.FinishScanningPacket
import com.cobblemon.mod.common.pokedex.scanner.PlayerScanningDetails
import com.cobblemon.mod.common.pokedex.scanner.PokedexUsageContext
import com.cobblemon.mod.common.pokedex.scanner.PokemonScanner
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

/**
 * Calls POKEMON_SCANNED event when pokemon is finished scanning
 *
 * @author Apion
 * @since August 10, 2024
 */
object FinishScanningHandler : ServerNetworkPacketHandler<FinishScanningPacket> {
    override fun handle(
        packet: FinishScanningPacket,
        server: MinecraftServer,
        player: ServerPlayer
    ) {
        val targetEntity = player.level().getEntity(packet.targetedId) ?: return

        if (PokemonScanner.isEntityInRange(player, targetEntity)) {
            val inProgressUUID = PlayerScanningDetails.playerToEntityMap[player.uuid]
            val progressTick = PlayerScanningDetails.playerToTickMap[player.uuid]
            val ticksScan = progressTick?.let { server.tickCount - it } ?: return
            if (targetEntity.uuid == inProgressUUID && ticksScan >= PokedexUsageContext.TICKS_TO_SCAN) {
                val pokemonEntity = targetEntity as? PokemonEntity ?: return
                val dex = Cobblemon.playerDataManager.getPokedexData(player)
                if (pokemonEntity.owner == player) {
                    dex.catch(pokemonEntity.pokemon)
                } else {
                    dex.encounter(pokemonEntity.pokemon)
                }
                CobblemonEvents.POKEMON_SCANNED.post(PokemonScannedEvent(player, pokemonEntity))
                ServerConfirmedScanPacket(pokemonEntity.pokemon.species.resourceIdentifier).sendToPlayer(player)
            }
        }
    }
}