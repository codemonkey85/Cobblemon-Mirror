package com.cobblemon.mod.common.client.net.pokedex

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.net.messages.client.pokedex.ServerConfirmedScanPacket
import net.minecraft.client.Minecraft

object ServerConfirmedScanHandler : ClientNetworkPacketHandler<ServerConfirmedScanPacket> {
    override fun handle(packet: ServerConfirmedScanPacket, client: Minecraft) {
        CobblemonClient.pokedexUsageContext.onServerConfirmedScan(packet)
    }

}