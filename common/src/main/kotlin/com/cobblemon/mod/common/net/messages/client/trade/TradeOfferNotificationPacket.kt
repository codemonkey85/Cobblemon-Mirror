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
import com.cobblemon.mod.common.util.readText
import com.cobblemon.mod.common.util.readUUID
import com.cobblemon.mod.common.util.writeText
import com.cobblemon.mod.common.util.writeUUID
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID
import net.minecraft.network.chat.MutableComponent

/**
 * Packet sent to the client to notify a player that someone requested to trade with them.
 *
 * @author Hiroku
 * @since March 6th, 2023
 */
class TradeOfferNotificationPacket(val tradeOfferId: UUID, val traderId: UUID, val traderName: MutableComponent): NetworkPacket<TradeOfferNotificationPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(tradeOfferId)
        buffer.writeUUID(traderId)
        buffer.writeText(traderName)
    }

    companion object {
        val ID = cobblemonResource("trade_offer_notification")
        fun decode(buffer: RegistryFriendlyByteBuf) = TradeOfferNotificationPacket(buffer.readUUID(), buffer.readUUID(), buffer.readText().copy())
    }
}