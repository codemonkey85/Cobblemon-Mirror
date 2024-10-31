/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.codec

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf

class MappedCodec<A : CodecMapped, K>(
    val codecRetriever: (K) -> Codec<out A>,
    val keyName: String = "type",
    val keyFromString: (String) -> K
) : Codec<A> {
    override fun <T> encode(input: A, ops: DynamicOps<T>, prefix: T): DataResult<T> {
        return input.encode(ops)
    }

    override fun <T> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<A, T>> {
        val thingCodec: Codec<ThingWithType> = RecordCodecBuilder.create { instance ->
            instance
                .group(PrimitiveCodec.STRING.fieldOf(keyName).forGetter { it.string })
                .apply(instance, ::ThingWithType)
        }
        val thingWithType = thingCodec.decode(ops, input).map { it.first }
        val key = thingWithType.getOrThrow().string
        val codec = codecRetriever(keyFromString(key))
        return codec.decode(ops, input).map { Pair(it.first, it.second) }
    }
}

private class ThingWithType(val string: String)

interface CodecMapped {
    fun <T> encode(ops: DynamicOps<T>): DataResult<T>
    fun readFromBuffer(buffer: RegistryFriendlyByteBuf)
    fun writeToBuffer(buffer: RegistryFriendlyByteBuf)
}
