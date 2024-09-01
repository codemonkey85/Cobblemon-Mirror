/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.net.serializers

import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.syncher.EntityDataSerializer
import net.minecraft.world.phys.Vec3

object Vec3DataSerializer : EntityDataSerializer<Vec3> {
    val ID = cobblemonResource("vec3d")
    fun write(buffer: RegistryFriendlyByteBuf, vec: Vec3) {
        buffer.writeDouble(vec.x)
        buffer.writeDouble(vec.y)
        buffer.writeDouble(vec.z)
    }

    fun read(buffer: RegistryFriendlyByteBuf) = Vec3(
        buffer.readDouble(),
        buffer.readDouble(),
        buffer.readDouble()
    )

    override fun copy(vec: Vec3) = Vec3(vec.x, vec.y, vec.z)
    override fun codec() = StreamCodec.of(::write, ::read)

}