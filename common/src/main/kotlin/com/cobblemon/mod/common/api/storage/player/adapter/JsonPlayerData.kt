/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.player.adapter

import com.cobblemon.mod.common.api.storage.player.PlayerData
import com.cobblemon.mod.common.api.storage.player.PlayerDataExtension
import com.cobblemon.mod.common.util.adapters.IdentifierAdapter
import com.cobblemon.mod.common.util.fromJson
import com.google.gson.GsonBuilder
import java.io.BufferedReader
import java.io.FileReader
import java.io.PrintWriter
import java.nio.file.Path
import java.util.UUID
import net.minecraft.server.MinecraftServer
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.LevelResource
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

class JsonPlayerData: PlayerDataStoreAdapter {

    companion object {
        val gson = GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .registerTypeAdapter(PlayerDataExtension::class.java, PlayerDataExtensionAdapter)
            .registerTypeAdapter(ResourceLocation::class.java, IdentifierAdapter)
            .create()
    }

    lateinit var savePath: Path
    var useNestedStructure = true

    fun setup(server: MinecraftServer) {
        savePath = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).parent
    }

    fun getSubFile(uuid: UUID): String {
        return if (useNestedStructure) {
            "${uuid.toString().substring(0, 2)}/$uuid.json"
        } else {
            "$uuid.json"
        }
    }
    private fun file(uuid: UUID) = savePath.resolve("cobblemonplayerdata/${getSubFile(uuid)}").toFile()

    override fun load(uuid: UUID): PlayerData {
        val playerFile = file(uuid)
        playerFile.parentFile.mkdirs()
        return if (playerFile.exists()) {
            gson.fromJson<PlayerData>(BufferedReader(FileReader(playerFile))).also {
                // Resolves old data that's missing new properties
                val newProps = it::class.memberProperties.filterIsInstance<KMutableProperty<*>>().filter { member -> member.getter.call(it) == null }
                if (newProps.isNotEmpty()) {
                    val defaultData = PlayerData.defaultData(uuid)
                    newProps.forEach { member -> member.setter.call(it, member.getter.call(defaultData)) }
                }
            }
        } else {
            PlayerData.defaultData(uuid).also(::save)
        }
    }

    override fun save(playerData: PlayerData) {
        val file = file(playerData.uuid)
        file.parentFile.mkdirs()
        val pw = PrintWriter(file(playerData.uuid))
        pw.write(gson.toJson(playerData))
        pw.flush()
        pw.close()
    }
}