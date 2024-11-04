/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.player.adapter

import com.cobblemon.mod.common.api.storage.player.InstancedPlayerData
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreType
import com.mongodb.client.MongoClient
import net.minecraft.server.MinecraftServer
import java.util.UUID

/**
 * A [PlayerDataStoreBackend] that stores the [InstancedPlayerData] in a file.
 *
 * @author Pebbles
 * @since October 27, 2024
 */
abstract class MongoBasedPlayerDataStoreBackend<T : InstancedPlayerData>(
    val mongoClient: MongoClient, val type: PlayerInstancedDataStoreType, databaseName: String, collectionName: String
) : PlayerDataStoreBackend<T>(type) {

    abstract val defaultData: (UUID) -> (T)
    val collection by lazy {
        mongoClient.getDatabase(databaseName).getCollection(collectionName)
    }

    override fun setup(server: MinecraftServer) {
        // Ping the database to ensure it's connected
        mongoClient.listDatabaseNames().first()
    }
}