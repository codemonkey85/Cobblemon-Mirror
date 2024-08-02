/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.serialization

import com.google.gson.JsonElement
import net.minecraft.nbt.Tag

/**
 * A serializer for the NBT and Json format.
 *
 * @author Licious
 * @since June 27th, 2022
 */
interface DataSerializer<N : Tag, J : JsonElement> {

    /**
     * Loads the given [N] into the object representation.
     *
     * @param nbt The [NbtElement] of type [N] this object is saved as.
     */
    fun loadFromNBT(nbt: N)

    /**
     * Saves the object into the NBT representation.
     *
     * @return The [NbtElement] with the type of [N].
     */
    fun saveToNBT(): N

    /**
     * Loads the given [J] into the object representation.
     *
     * @param json The [JsonElement] of type [J] this object is saved as.
     */
    fun loadFromJson(json: J)

    /**
     * Saves the object into the JSON representation.
     *
     * @return The [JsonElement] with the type of [J].
     */
    fun saveToJson(): J

}