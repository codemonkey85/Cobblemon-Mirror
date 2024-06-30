/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.net.serializers

import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.entity.data.TrackedDataHandler
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.util.Identifier

/**
 * Data serializer of [Identifier] for DataTracker things.
 *
 * @author Hiroku
 * @since May 22nd, 2023
 */
object IdentifierDataSerializer : TrackedDataHandler<Identifier> {
    val ID = cobblemonResource("identifier")
    override fun copy(value: Identifier) = Identifier.of(value.namespace, value.path)
    fun read(buf: PacketByteBuf) = Identifier.of(buf.readString(), buf.readString())
    fun write(buf: PacketByteBuf, value: Identifier) {
        buf.writeString(value.namespace)
        buf.writeString(value.path)
    }

    override fun codec() = PacketCodec.ofStatic(::write, ::read)
}