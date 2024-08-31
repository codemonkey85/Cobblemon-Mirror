/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.data

import com.cobblemon.mod.common.Cobblemon
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager

/**
 * A [DataRegistry] that consumes JSON files.
 * Every deserialized instance is attached to an [ResourceLocation].
 * For example a file under data/mymod/[resourcePath]/entry.json would be backed by the identifier modid:entry.
 *
 * @param T The type of the data consumed by this registry.
 *
 * @author Licious
 * @since August 5th, 2022
 */
interface JsonDataRegistry<T> : DataRegistry {

    /**
     * The [Gson] used to deserialize the data this registry will consume.
     */
    val gson: Gson

    /**
     * The [TypeToken] of type [T].
     */
    val typeToken: TypeToken<T>

    /**
     * The folder location for the data this registry will consume.
     */
    val resourcePath: String

    override fun reload(manager: ResourceManager) {
        this.reload(Cobblemon.implementation.reloadJsonRegistry(this, manager))
    }

    /**
     * Reloads this registry from the deserialized data.
     *
     * @param data A map of the data associating an instance to the respective identifier from the [ResourceManager].
     */
    fun reload(data: Map<ResourceLocation, T>)

    companion object {
        const val JSON_EXTENSION = ".json"
    }
}