/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.net.serializers

import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.*
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.syncher.EntityDataSerializer

object StringSetDataSerializer : EntityDataSerializer<Set<String>> {
    val ID = cobblemonResource("string_set")
    fun write(buffer: RegistryFriendlyByteBuf, set: Set<String>) {
        buffer.writeSizedInt(IntSize.U_BYTE, set.size)
        set.forEach(buffer::writeString)
    }

    fun read(buffer: RegistryFriendlyByteBuf): Set<String> {
        val set = mutableSetOf<String>()
        repeat(times = buffer.readSizedInt(IntSize.U_BYTE)) {
            set.add(buffer.readString())
        }
        return set
    }

    override fun copy(set: Set<String>) = set.toSet()
    override fun codec(): StreamCodec<in RegistryFriendlyByteBuf, Set<String>> = StreamCodec.of(::write, ::read)
}