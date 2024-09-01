/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.dialogue

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.readUUID
import com.cobblemon.mod.common.util.writeString
import com.cobblemon.mod.common.util.writeUUID
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

/**
 * Sent by the client to the server when they are providing some kind of input to the dialogue to progress it.
 * This could be a text input, or a button press, etc. depending on the input of the active dialogue.
 *
 * @author Hiroku
 * @since December 29th, 2023
 */
class InputToDialoguePacket(val inputId: UUID, val input: String = ""): NetworkPacket<InputToDialoguePacket> {
    companion object {
        val ID = cobblemonResource("input_to_dialogue")
        fun decode(buffer: RegistryFriendlyByteBuf) = InputToDialoguePacket(
            buffer.readUUID(),
            buffer.readString()
        )
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(inputId)
        buffer.writeString(input)
    }
}