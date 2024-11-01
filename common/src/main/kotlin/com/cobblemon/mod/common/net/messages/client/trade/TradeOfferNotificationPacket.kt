/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.trade

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.trade.TradeManager
import com.cobblemon.mod.common.util.*
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

/**
 * Packet sent to the client to notify a player that someone requested to trade with them.
 *
 * Handled by [com.cobblemon.mod.common.client.net.trade.TradeOfferNotificationHandler].
 *
 * @param requestID The unique identifier of the request.
 * @param senderID The unique identifier of the player who sent the request.
 * @param expiryTime How long (in seconds) the offer is active.
 *
 * @author Hiroku
 * @since March 6th, 2023
 */
class TradeOfferNotificationPacket(val requestID: UUID, val senderID: UUID, val expiryTime: Int): NetworkPacket<TradeOfferNotificationPacket> {
    override val id = ID

    constructor(request: TradeManager.TradeRequest) : this(
        requestID = request.requestID,
        senderID = request.senderID,
        expiryTime = request.expiryTime
    )

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(requestID)
        buffer.writeUUID(senderID)
        buffer.writeInt(expiryTime)
    }

    companion object {
        val ID = cobblemonResource("trade_offer_notification")
        fun decode(buffer: RegistryFriendlyByteBuf) = TradeOfferNotificationPacket(buffer.readUUID(), buffer.readUUID(), buffer.readInt())
    }
}