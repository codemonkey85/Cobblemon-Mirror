/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.npc

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Packet sent to the client to close the currently open NPC editor, if one is open.
 *
 * @author Hiroku
 * @since February 4th, 2024
 */
class CloseNPCEditorPacket : NetworkPacket<CloseNPCEditorPacket> {
    companion object {
        val ID = cobblemonResource("close_npc_editor")
        fun decode(buffer: RegistryFriendlyByteBuf) = CloseNPCEditorPacket()
    }
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {}
}