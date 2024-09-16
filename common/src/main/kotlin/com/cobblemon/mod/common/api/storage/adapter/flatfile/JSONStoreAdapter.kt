/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.adapter.flatfile

import com.cobblemon.mod.common.api.storage.PokemonStore
import com.cobblemon.mod.common.api.storage.StorePosition
import com.cobblemon.mod.common.util.fromJson
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.PrintWriter
import java.util.UUID
import net.minecraft.core.RegistryAccess

/**
 * A [FileStoreAdapter] for JSON files. This allows a [PokemonStore] to be serialized to a .json file. This is usually
 * slower and makes for a larger file per storage by several times compared to a [NBTStoreAdapter].
 *
 * @author Hiroku
 * @since November 29th, 2021
 */
open class JSONStoreAdapter(
    rootFolder: String,
    useNestedFolders: Boolean,
    folderPerClass: Boolean,
    private val gson: Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(),
) : OneToOneFileStoreAdapter<JsonObject>(rootFolder, useNestedFolders, folderPerClass, "json") {
    override fun <E : StorePosition, T : PokemonStore<E>> serialize(store: T, registryAccess: RegistryAccess) = store.saveToJSON(JsonObject(), registryAccess)

    override fun save(file: File, serialized: JsonObject) {
        val pw = PrintWriter(file)
        val json = gson.toJson(serialized)
        pw.write(json)
        pw.flush()
        pw.close()
    }

    override fun <E, T : PokemonStore<E>> load(file: File, storeClass: Class<out T>, uuid: UUID, registryAccess: RegistryAccess): T? {
        return try {
            val br = BufferedReader(FileReader(file))
            val json = gson.fromJson<JsonObject>(br)
            br.close()
            val store = try {
                storeClass.getConstructor(UUID::class.java, UUID::class.java).newInstance(uuid, uuid)
            } catch (exception: NoSuchMethodException) {
                storeClass.getConstructor(UUID::class.java).newInstance(uuid)
            }
            store.loadFromJSON(json, registryAccess)
            store
        } catch (e: Exception) {
            null
        }
    }
}