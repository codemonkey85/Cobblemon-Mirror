/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.neoforge.net

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.NetworkManager
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.client.net.data.DataRegistrySyncPacketHandler
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.registration.HandlerThread

// https://neoforged.net/news/20.4networking-rework/
object CobblemonNeoForgeNetworkManager : NetworkManager {
    const val PROTOCOL_VERSION = "1.0.0"

    fun registerMessages(event: RegisterPayloadHandlersEvent) {
        val registrar = event
            .registrar(Cobblemon.MODID)
            .versioned(PROTOCOL_VERSION)

        val netRegistrar = event
            .registrar(Cobblemon.MODID)
            .versioned(PROTOCOL_VERSION)
            .executesOn(HandlerThread.NETWORK)

        val syncPackets = HashSet<ResourceLocation>()
        val asyncPackets = HashSet<ResourceLocation>()

        CobblemonNetwork.s2cPayloads.map { NeoForgePacketInfo(it) }.forEach {
            val handleAsync = it.info.handler is DataRegistrySyncPacketHandler<*, *>
            if (handleAsync) asyncPackets += it.info.id
            else syncPackets += it.info.id

            it.registerToClient(if (handleAsync) netRegistrar else registrar)
        }
        CobblemonNetwork.c2sPayloads.map { NeoForgePacketInfo(it) }.forEach { it.registerToServer(registrar) }

        Cobblemon.LOGGER.info("Async packets: $asyncPackets")
        Cobblemon.LOGGER.info("Sync packets: $syncPackets")
    }

    override fun sendPacketToPlayer(player: ServerPlayer, packet: NetworkPacket<*>) {
        player.connection.send(packet)
    }

    override fun sendToServer(packet: NetworkPacket<*>) {
        Minecraft.getInstance().connection?.send(packet)
    }
}