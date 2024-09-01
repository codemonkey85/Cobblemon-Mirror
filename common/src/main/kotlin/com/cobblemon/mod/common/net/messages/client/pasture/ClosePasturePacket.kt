/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pasture

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Packet sent to close the pasture GUI if it's open. Used when the block is broken.
 *
 * @author Hiroku
 * @since April 17th, 2023
 */
class ClosePasturePacket : NetworkPacket<ClosePasturePacket> {
    companion object {
        val ID = cobblemonResource("close_pasture")
        fun decode(buffer: RegistryFriendlyByteBuf) = ClosePasturePacket()
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {}
}