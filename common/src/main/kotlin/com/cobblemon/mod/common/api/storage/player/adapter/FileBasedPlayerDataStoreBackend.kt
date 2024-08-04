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
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.LevelResource
import java.nio.file.Path
import java.util.UUID

/**
 * A [PlayerDataStoreBackend] that stores the [InstancedPlayerData] in a file.
 *
 * @author Apion
 * @since February 21, 2024
 */
abstract class FileBasedPlayerDataStoreBackend<T : InstancedPlayerData>(
    val subfolder: String,
    val type: PlayerInstancedDataStoreType,
    val fileExt: String
) : PlayerDataStoreBackend<T>(type) {

    abstract val defaultData: (UUID) -> (T)
    lateinit var savePath: Path
    val useNestedStructure = true

    override fun setup(server: MinecraftServer) {
        savePath = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).parent
    }

    //TODO: Set file extension to be a field
    fun getSubFile(uuid: UUID): String {
        return if (useNestedStructure) {
            "${uuid.toString().substring(0, 2)}/$uuid.$fileExt"
        } else {
            "$uuid.$fileExt"
        }
    }

    fun filePath(uuid: UUID) = savePath.resolve("$subfolder/${getSubFile(uuid)}").toFile()

}