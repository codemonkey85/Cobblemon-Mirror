package com.cobblemon.mod.common.net.serverhandling.pokedex.scanner

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.net.messages.server.pokedex.scanner.StartScanningPacket
import com.cobblemon.mod.common.pokedex.scanner.PlayerScanningDetails
import com.cobblemon.mod.common.pokedex.scanner.PokemonScanner
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import java.time.Instant

object StartScanningHandler : ServerNetworkPacketHandler<StartScanningPacket> {
    override fun handle(
        packet: StartScanningPacket,
        server: MinecraftServer,
        player: ServerPlayer
    ) {
        val targetEntity = player.level().getEntity(packet.targetedId) ?: return

        if (PokemonScanner.isEntityInRange(player, targetEntity)) {
            PlayerScanningDetails.playerToEntityMap[player.uuid] = targetEntity.uuid
            PlayerScanningDetails.playerToTickMap[player.uuid] = server.tickCount
        }
    }
}