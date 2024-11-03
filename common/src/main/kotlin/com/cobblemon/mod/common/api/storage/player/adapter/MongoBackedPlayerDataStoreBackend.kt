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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mongodb.client.MongoClient
import com.mongodb.client.model.ReplaceOptions
import org.bson.Document
import java.io.BufferedReader
import java.io.FileReader
import java.io.PrintWriter
import java.util.UUID
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

abstract class MongoBackedPlayerDataStoreBackend<T : InstancedPlayerData>(
    mongoClient: MongoClient, databaseName: String, collectionName: String, type: PlayerInstancedDataStoreType
) : MongoBasedPlayerDataStoreBackend<T>(mongoClient, type, databaseName, collectionName) {
    abstract val gson: Gson

    //The class GSON needs to deserialize to
    abstract val classToken: TypeToken<T>

    override fun save(playerData: T) {
        collection.replaceOne(
            Document("uuid", playerData.uuid.toString()),
            Document.parse(gson.toJson(playerData)),
            ReplaceOptions().upsert(true)
        )
    }

    override fun load(uuid: UUID): T {
        val filter = Document("uuid", uuid.toString())
        val document = collection.find(filter).first()

        return if (document != null) {
            val jsonStr = document.toJson()
            gson.fromJson(jsonStr, classToken).also {
                val newProps = it::class.memberProperties.filterIsInstance<KMutableProperty<*>>()
                    .filter { member -> member.getter.call(it) == null }
                if (newProps.isNotEmpty()) {
                    val defaultData = defaultData(uuid)
                    newProps.forEach { member -> member.setter.call(it, member.getter.call(defaultData)) }
                }
            }
        } else {
            defaultData(uuid).also(::save)
        }
    }
}