/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.trade

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readUUID
import com.cobblemon.mod.common.util.writeUUID
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

class ChangeTradeAcceptancePacket(val pokemonOfferId: UUID, val newAcceptance: Boolean) : NetworkPacket<ChangeTradeAcceptancePacket> {
    companion object {
        val ID = cobblemonResource("accept_trade")
        fun decode(buffer: RegistryFriendlyByteBuf) = ChangeTradeAcceptancePacket(buffer.readUUID(), buffer.readBoolean())
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(pokemonOfferId)
        buffer.writeBoolean(newAcceptance)
    }
}