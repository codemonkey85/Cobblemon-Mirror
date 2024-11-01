/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.trade

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.trade.TradeManager.TradeRequest
import com.cobblemon.mod.common.net.messages.client.trade.TradeOfferNotificationPacket
import com.cobblemon.mod.common.net.serverhandling.trade.AcceptTradeRequestHandler
import com.cobblemon.mod.common.util.cobblemonResource
import java.util.UUID
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Packet sent when a player accepts a [TradeRequest] after receiving the respective [TradeOfferNotificationPacket].
 *
 * Handled by [AcceptTradeRequestHandler].
 *
 * @param requestID The unique identifier of the request that the player is responding to.
 * @param accept Whether the player accepted the team request.
 *
 * @author Hiroku
 * @since March 12th, 2023
 */
class AcceptTradeRequestPacket(val tradeOfferId: UUID) : NetworkPacket<AcceptTradeRequestPacket> {
    companion object {
        val ID = cobblemonResource("accept_trade_request")
        fun decode(buffer: RegistryFriendlyByteBuf) = AcceptTradeRequestPacket(buffer.readUUID())
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(tradeOfferId)
    }
}