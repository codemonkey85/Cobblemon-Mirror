/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.dialogue

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.writeNullable
import com.cobblemon.mod.common.util.writeUUID
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

/**
 * Packet sent to the client to close the active dialogue.
 *
 * @param dialogueId The ID of the dialogue to close. If null, closes the active dialogue.
 * @author Hiroku
 * @since December 27th, 2023
 */
class DialogueClosedPacket(val dialogueId: UUID? = null) : NetworkPacket<DialogueClosedPacket> {
    companion object {
        val ID = cobblemonResource("dialogue_closed")
        fun decode(buffer: RegistryFriendlyByteBuf) = DialogueClosedPacket(buffer.readNullable { it.readUUID() })
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeNullable(dialogueId) { buff, value -> buff.writeUUID(value) }
    }
}