/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.storage.pc

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.net.serverhandling.storage.pc.UnlinkPlayerFromPCHandler
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Packet sent to remove the PC link for the player.
 *
 * Handled by [UnlinkPlayerFromPCHandler].
 *
 * @author Village
 * @since January 18th, 2023
 */
class UnlinkPlayerFromPCPacket : NetworkPacket<UnlinkPlayerFromPCPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {}
    companion object {
        val ID = cobblemonResource("unlink_player_from_pc")
        fun decode(buffer: RegistryFriendlyByteBuf) = UnlinkPlayerFromPCPacket()
    }
}