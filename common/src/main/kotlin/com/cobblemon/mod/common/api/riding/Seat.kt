/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding

import com.cobblemon.mod.common.api.net.Encodable
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.world.phys.Vec3

/**
 * Seat Properties are responsible for the base information that would then be used to construct a Seat on an entity.
 */
data class Seat(
    val locator: String = "seat1",
    val offset: Vec3 = Vec3.ZERO
) : Encodable {
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(locator)
        buffer.writeDouble(this.offset.x)
        buffer.writeDouble(this.offset.y)
        buffer.writeDouble(this.offset.z)
    }

    companion object {
        fun decode(buffer: RegistryFriendlyByteBuf) : Seat {
            return Seat(
                buffer.readString(),
                Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble())
            )
        }
    }
}
