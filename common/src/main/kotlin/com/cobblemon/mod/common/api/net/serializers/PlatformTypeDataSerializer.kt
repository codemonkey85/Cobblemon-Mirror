/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.net.serializers

import com.cobblemon.mod.common.entity.PlatformType
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.syncher.EntityDataSerializer

object PlatformTypeDataSerializer : EntityDataSerializer<PlatformType> {
    // TODO: This ID only looks it's used by NeoForge and that hasn't been tested yet
    val ID = cobblemonResource("platform_type")
    fun read(buf: RegistryFriendlyByteBuf) = PlatformType.entries[buf.readInt()]
    override fun copy(value: PlatformType) = value
    fun write(buf: RegistryFriendlyByteBuf, value: PlatformType) {
        buf.writeInt(value.ordinal)
    }

    override fun codec() = StreamCodec.of(::write, ::read)
}