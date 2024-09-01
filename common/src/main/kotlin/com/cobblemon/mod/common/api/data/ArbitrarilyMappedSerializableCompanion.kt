/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.data

import com.cobblemon.mod.common.api.codec.CodecMapped
import com.cobblemon.mod.common.api.codec.MappedCodec
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import com.mojang.serialization.Codec
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * A utility class to help give Codec support for map adapted class hierarchies.
 *
 * @author Hiroku
 * @since January 21st, 2023
 */
abstract class ArbitrarilyMappedSerializableCompanion<T : CodecMapped, K>(
    val keyFromString: (String) -> K,
    val stringFromKey: (K) -> String,
    val keyFromValue: (T) -> K
) {
    val codec: MappedCodec<T, K> = MappedCodec(codecRetriever = { subtypes[it]!!.codec }, keyFromString = keyFromString)
    private val subtypes = mutableMapOf<K, RegisteredSubtype<out T>>()

    fun <E : T> registerSubtype(key: K, clazz: Class<E>, codec: Codec<E>) {
        subtypes[key] = RegisteredSubtype(clazz, codec)
    }

    fun writeToBuffer(buffer: RegistryFriendlyByteBuf, value: T) {
        val typeString = stringFromKey(keyFromValue(value))
        buffer.writeString(typeString)
        value.writeToBuffer(buffer)
    }

    fun readFromBuffer(buffer: RegistryFriendlyByteBuf): T {
        val typeString = buffer.readString()
        val clazz = subtypes[keyFromString(typeString)]?.clazz ?: throw IllegalArgumentException("Unrecognized subtype: $typeString")
        val value = clazz.getDeclaredConstructor().newInstance()
        value.readFromBuffer(buffer)
        return value
    }
}

class RegisteredSubtype<T>(val clazz: Class<out T>, val codec: Codec<out T>)