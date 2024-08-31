/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.net

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.util.server
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerPlayer
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level

/**
 * Platform abstract blueprint of a packet being sent out.
 * The handling of encoding, decoding and resolving the packet is done on the individual platform implementations.
 *
 * @author Hiroku, Licious
 * @since November 27th, 2021
 */
interface NetworkPacket<T: NetworkPacket<T>> : CustomPacketPayload, Encodable {

    /**
     *
     */
    val id: ResourceLocation

    /**
     * TODO
     *
     * @param player
     */
    fun sendToPlayer(player: ServerPlayer) = CobblemonNetwork.sendPacketToPlayer(player, this)

    /**
     * TODO
     *
     * @param players
     */
    fun sendToPlayers(players: Iterable<ServerPlayer>) {
        if (players.any()) {
            CobblemonNetwork.sendPacketToPlayers(players, this)
        }
    }

    /**
     * TODO
     *
     */
    fun sendToAllPlayers() = CobblemonNetwork.sendToAllPlayers(this)

    /**
     * TODO
     *
     */
    fun sendToServer() = CobblemonNetwork.sendToServer(this)

    // A copy from PlayerManager#sendToAround to work with our packets
    /**
     * TODO
     *
     * @param x
     * @param y
     * @param z
     * @param distance
     * @param worldKey
     * @param exclusionCondition
     */
    fun sendToPlayersAround(x: Double, y: Double, z: Double, distance: Double, worldKey: ResourceKey<Level>, exclusionCondition: (ServerPlayer) -> Boolean = { false }) {
        val server = server() ?: return
        server.playerList.players.filter { player ->
            if (exclusionCondition.invoke(player))
                return@filter false
            val xDiff = x - player.x
            val yDiff = y - player.y
            val zDiff = z - player.z
            return@filter (xDiff * xDiff + yDiff * yDiff + zDiff) < distance * distance
        }
        .forEach { player -> CobblemonNetwork.sendPacketToPlayer(player, this) }
    }

    override fun type() = CustomPacketPayload.Type<T>(id)
}