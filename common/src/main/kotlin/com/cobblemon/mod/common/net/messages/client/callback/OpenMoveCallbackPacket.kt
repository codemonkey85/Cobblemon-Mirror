/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.callback

import com.cobblemon.mod.common.api.callback.MoveSelectDTO
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.chat.MutableComponent
import java.util.*

/**
 * Packet send to the client to force them to open a move selection GUI.
 *
 * @author Hiroku
 * @since June 30th, 2023
 */
class OpenMoveCallbackPacket(val uuid: UUID, val title: MutableComponent, val moves: List<MoveSelectDTO>) : NetworkPacket<OpenMoveCallbackPacket> {
    companion object {
        val ID = cobblemonResource("open_move_callback")
        fun decode(buffer: RegistryFriendlyByteBuf) = OpenMoveCallbackPacket(
            uuid = buffer.readUUID(),
            title = ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.decode(buffer).copy(),
            moves = buffer.readList { _ -> MoveSelectDTO(buffer) }
        )
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(uuid)
        ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.encode(buffer, title)
        buffer.writeCollection(moves) { _, v -> v.writeToBuffer(buffer) }
    }
}