/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.animation

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

/**
 * Packet sent to arbitrarily play the first animation of the given set that exists on the given entity. MoLang
 * expressions are also sent to setup any MoLang variables that are needed for the animation.
 *
 * @author Hiroku
 * @since October 21st, 2023
 */
class PlayPosableAnimationPacket(
    val entityId: Int,
    val animation: Set<String>,
    val expressions: List<String>
) : NetworkPacket<PlayPosableAnimationPacket> {
    override val id: ResourceLocation = ID

    companion object {
        val ID = cobblemonResource("play_posable_animation")
        fun decode(buffer: RegistryFriendlyByteBuf) = PlayPosableAnimationPacket(
            buffer.readInt(),
            buffer.readList { buffer.readString() }.toSet(),
            buffer.readList { buffer.readString() }
        )
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeInt(entityId)
        buffer.writeCollection(animation) { pb, value -> pb.writeString(value) }
        buffer.writeCollection(expressions) { pb, value -> pb.writeString(value) }
    }
}