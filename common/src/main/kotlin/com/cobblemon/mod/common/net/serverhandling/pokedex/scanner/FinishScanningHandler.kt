/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.pokedex.scanner

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.events.CobblemonEvents.POKEMON_SCANNED
import com.cobblemon.mod.common.api.events.pokedex.scanning.PokemonScannedEvent
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.pokedex.ServerConfirmedRegisterPacket
import com.cobblemon.mod.common.net.messages.server.pokedex.scanner.FinishScanningPacket
import com.cobblemon.mod.common.pokedex.scanner.PlayerScanningDetails
import com.cobblemon.mod.common.pokedex.scanner.PokedexUsageContext
import com.cobblemon.mod.common.pokedex.scanner.PokemonScanner
import com.cobblemon.mod.common.pokedex.scanner.ScannableEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

/**
 * Calls [POKEMON_SCANNED] event when Pokémon is finished scanning
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
        if (PokemonScanner.isEntityInRange(player, targetEntity, packet.zoomLevel)) {
            val inProgressUUID = PlayerScanningDetails.playerToEntityMap[player.uuid]
            val progressTick = PlayerScanningDetails.playerToTickMap[player.uuid]
            val ticksScan = progressTick?.let { server.tickCount - it } ?: return
            if (targetEntity.uuid == inProgressUUID && ticksScan >= PokedexUsageContext.SUCCESS_SCAN_SERVER_TICKS) {
                val scannableEntity = targetEntity as? ScannableEntity ?: return
                val dex = Cobblemon.playerDataManager.getPokedexData(player)
                val pokedexEntityData = scannableEntity.resolvePokemonScan()
                if(pokedexEntityData != null){
                    val newInformation = dex.getNewInformation(pokedexEntityData)
                    if ((scannableEntity as? PokemonEntity)?.owner === player) {
                        dex.catch(scannableEntity.pokemon)
                    } else {
                        dex.encounter(pokedexEntityData)
                    }

                    POKEMON_SCANNED.post(PokemonScannedEvent(player, pokedexEntityData, scannableEntity))
                    ServerConfirmedRegisterPacket(pokedexEntityData.species.resourceIdentifier, newInformation).sendToPlayer(player)
                }
            }
        }
    }
}