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
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Sent by the client to the server when the player wants to escape the current dialogue.
 *
 * @author Hiroku
 * @since December 29th, 2023
 */
class EscapeDialoguePacket : NetworkPacket<EscapeDialoguePacket> {
    companion object {
        val ID = cobblemonResource("escape_dialogue")
        fun decode(buffer: RegistryFriendlyByteBuf) = EscapeDialoguePacket()
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {}
}