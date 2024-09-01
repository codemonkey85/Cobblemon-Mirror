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
 * Packet sent to close the trade GUI as the trade was cancelled (programmatically, by log-off, or by player action).
 *
 * Handled by [com.cobblemon.mod.common.client.net.trade.TradeCancelledHandler]
 *
 * @author Hiroku
 * @since March 5th, 2023
 */
class TradeCancelledPacket : NetworkPacket<TradeCancelledPacket> {
    companion object {
        val ID = cobblemonResource("trade_cancelled")
        fun decode(buffer: RegistryFriendlyByteBuf) = TradeCancelledPacket()
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {}
}