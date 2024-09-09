/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.player.factory

import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.storage.player.InstancedPlayerData
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataFactory
import com.cobblemon.mod.common.api.storage.player.adapter.PlayerDataStoreBackend
import com.cobblemon.mod.common.net.messages.client.SetClientPlayerDataPacket
import com.cobblemon.mod.common.util.getPlayer
import com.cobblemon.mod.common.util.removeIf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import java.util.UUID

/**
 * A type of data store that keeps a cache, entries are evicted when the cached player disconnects from the server
 */
class CachedPlayerDataStoreFactory<T : InstancedPlayerData>(val backend: PlayerDataStoreBackend<T>) : PlayerInstancedDataFactory<T> {

    private val cache = mutableMapOf<UUID, T>()

    override fun setup(server: MinecraftServer) {
        backend.setup(server);
    }

    override fun getForPlayer(playerId: UUID): T {
        return if (cache.contains(playerId))
            cache[playerId]!!;
        else {
            val data = backend.load(playerId);
            cache[playerId] = data
            data
        }
    }



    override fun saveAll() {
        cache.forEach { (_, pd) -> backend.save(pd) }
        cache.removeIf { (uuid, _) -> uuid.getPlayer() == null }
    }

    override fun saveSingle(playerId: UUID) {
        backend.save(getForPlayer(playerId))
    }

    override fun onPlayerDisconnect(player: ServerPlayer) {
        saveSingle(player.uuid)
        cache.remove(player.uuid)
    }

    override fun sendToPlayer(player: ServerPlayer) {
        player.sendPacket(SetClientPlayerDataPacket(backend.dataType, getForPlayer(player).toClientData()))
    }

}