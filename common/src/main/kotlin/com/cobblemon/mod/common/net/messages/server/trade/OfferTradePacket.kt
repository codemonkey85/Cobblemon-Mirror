/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.trade

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.net.serverhandling.trade.OfferTradeHandler
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

/**
 * Packet fired when a player makes an interaction request to trade with another player.
 *
 * Handled by [OfferTradeHandler].
 *
 * @param targetedEntityId The ID of the player who's the target of this interaction request.
 *
 * @author Hiroku
 * @since March 12th, 2023
 */
class OfferTradePacket(val offeredPlayerId: UUID) : NetworkPacket<OfferTradePacket> {
    companion object {
        val ID = cobblemonResource("offer_trade")
        fun decode(buffer: RegistryFriendlyByteBuf) = OfferTradePacket(buffer.readUUID())
    }
    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(offeredPlayerId)
    }
}