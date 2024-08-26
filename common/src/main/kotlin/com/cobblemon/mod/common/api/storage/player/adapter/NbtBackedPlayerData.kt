/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.player.adapter

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.storage.player.InstancedPlayerData
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreType
import com.mojang.serialization.Codec
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtOps
import java.io.DataOutputStream
import java.io.FileOutputStream
import java.util.UUID

abstract class NbtBackedPlayerData<T : InstancedPlayerData>(
    subfolder: String,
    type: PlayerInstancedDataStoreType
) : FileBasedPlayerDataStoreBackend<T>(subfolder, type, "nbt") {
    abstract val codec: Codec<T>
    override fun save(playerData: T) {
        val file = filePath(playerData.uuid)
        file.parentFile.mkdirs()
        val os = DataOutputStream(FileOutputStream(filePath(playerData.uuid)))
        val encodeResult = NbtOps.INSTANCE.withEncoder(codec).apply(playerData)
        NbtIo.write(encodeResult.result().get() as CompoundTag, os)
        os.flush()
        os.close()
    }

    override fun load(uuid: UUID): T {
        val playerFile = filePath(uuid)
        playerFile.parentFile.mkdirs()
        return if (playerFile.exists()) {
            val input = NbtIo.read(playerFile.toPath())
            val decodeResult = NbtOps.INSTANCE.withDecoder(codec).apply(input)
            decodeResult.getPartialOrThrow {
                Cobblemon.LOGGER.error("Error decoding pokedex for player uuid ${uuid}")
                Cobblemon.LOGGER.error(it)
                throw UnsupportedOperationException()
            }.first
        } else {
            defaultData.invoke(uuid).also(::save)
        }.also { it.initialize() }
    }
}