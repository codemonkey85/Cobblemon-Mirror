/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.trade

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Packet sent to the client when the trade has started.
 *
 * Handled by [com.cobblemon.mod.common.client.net.trade.TradeProcessStartedHandler]
 */
class TradeProcessStartedPacket(val isTradeInitiator: Boolean) : NetworkPacket<TradeProcessStartedPacket> {
    companion object {
        val ID = cobblemonResource("trade_process_started")
        fun decode(buffer: RegistryFriendlyByteBuf) = TradeProcessStartedPacket(buffer.readBoolean())
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeBoolean(isTradeInitiator)
    }
}