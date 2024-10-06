/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding

import com.cobblemon.mod.common.api.net.Encodable
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.world.phys.Vec3

/**
 * Seat Properties are responsible for the base information that would then be used to construct a Seat on an entity.
 */
data class Seat(
    val locator: String = "seat1",
    val offset: Vec3 = Vec3.ZERO,
    val poseOffsets: MutableList<SeatPoseOffset> = mutableListOf()
) : Encodable {
    fun getOffset(poseType: PoseType) : Vec3 {
        return poseOffsets.firstOrNull { poseType in it.poseTypes }?.offset ?: offset
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(locator)
        buffer.writeDouble(offset.x)
        buffer.writeDouble(offset.y)
        buffer.writeDouble(offset.z)
        buffer.writeCollection(poseOffsets) { _, offset -> offset.encode(buffer) }
    }

    companion object {
        fun decode(buffer: RegistryFriendlyByteBuf) : Seat {
            return Seat(
                buffer.readString(),
                Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                buffer.readList { SeatPoseOffset.decode(buffer) }
            )
        }
    }
}

class SeatPoseOffset {
    val poseTypes = mutableSetOf<PoseType>()
    var offset = Vec3.ZERO

    fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeCollection(poseTypes) { _, poseType -> buffer.writeString(poseType.name) }
        buffer.writeDouble(offset.x)
        buffer.writeDouble(offset.y)
        buffer.writeDouble(offset.z)
    }

    companion object {
        fun decode(buffer: RegistryFriendlyByteBuf) : SeatPoseOffset {
            val offset = SeatPoseOffset()
            offset.poseTypes.addAll(buffer.readList { buffer.readString().let { PoseType.valueOf(it) } })
            offset.offset = Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble())
            return offset
        }
    }
}
