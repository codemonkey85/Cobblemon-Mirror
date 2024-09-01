/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.net.serializers

import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.syncher.EntityDataSerializer

object PoseTypeDataSerializer : EntityDataSerializer<PoseType> {
    val ID = cobblemonResource("pose_type")
    fun read(buf: RegistryFriendlyByteBuf) = PoseType.values()[buf.readInt()]
    override fun copy(value: PoseType) = value
    fun write(buf: RegistryFriendlyByteBuf, value: PoseType) {
        buf.writeInt(value.ordinal)
    }

    override fun codec() = StreamCodec.of(::write, ::read)
}