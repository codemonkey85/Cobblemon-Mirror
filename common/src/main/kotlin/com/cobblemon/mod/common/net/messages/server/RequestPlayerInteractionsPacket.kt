/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import java.util.UUID
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Sent from client to request a [com.cobblemon.mod.common.net.messages.client.PlayerInteractOptionsPacket]
 *
 * @author Apion
 * @since November 5th, 2023
 */

class RequestPlayerInteractionsPacket(
    val targetId: UUID,
    val targetNumericId: Int,
    val pokemonId: UUID
) : NetworkPacket<RequestPlayerInteractionsPacket> {
    companion object {
        val ID = cobblemonResource("request_interactions")
        fun decode(buffer: RegistryFriendlyByteBuf) = RequestPlayerInteractionsPacket(buffer.readUUID(), buffer.readInt(), buffer.readUUID())
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(targetId)
        buffer.writeInt(targetNumericId)
        buffer.writeUUID(pokemonId)
    }

}