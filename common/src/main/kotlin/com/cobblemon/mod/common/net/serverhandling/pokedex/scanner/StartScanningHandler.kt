package com.cobblemon.mod.common.net.serverhandling.pokedex.scanner

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.net.messages.server.pokedex.scanner.StartScanningPacket
import com.cobblemon.mod.common.pokedex.scanner.PlayerScanningDetails
import com.cobblemon.mod.common.pokedex.scanner.PokemonScanner
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object StartScanningHandler : ServerNetworkPacketHandler<StartScanningPacket> {
    override fun handle(
        packet: StartScanningPacket,
        server: MinecraftServer,
        player: ServerPlayer
    ) {
        val targetedPokemon = PokemonScanner.findPokemon(player)
        if (targetedPokemon != null) {
            PlayerScanningDetails.playerToEntityMap[player.uuid] = packet.targetedUuid
            println("Targeted Pokemon: ${targetedPokemon.pokemon}")
        }


    }
}