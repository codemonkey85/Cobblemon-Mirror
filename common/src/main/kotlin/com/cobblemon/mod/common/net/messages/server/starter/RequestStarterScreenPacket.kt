/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.starter

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Packet sent by the client when they are requesting to choose a starter. The response
 * should probably be a packet instructing the starter screen to open.
 *
 * @author Hiroku
 * @since August 1st, 2022
 */
class RequestStarterScreenPacket : NetworkPacket<RequestStarterScreenPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {}

    companion object {
        val ID = cobblemonResource("request_starter_screen")
        fun decode(buffer: RegistryFriendlyByteBuf): RequestStarterScreenPacket = RequestStarterScreenPacket()
    }
}