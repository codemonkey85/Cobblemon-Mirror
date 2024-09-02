/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.trade

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.storage.party.PartyPosition
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readNullable
import com.cobblemon.mod.common.util.readPartyPosition
import com.cobblemon.mod.common.util.readUUID
import com.cobblemon.mod.common.util.writeNullable
import com.cobblemon.mod.common.util.writePartyPosition
import com.cobblemon.mod.common.util.writeUUID
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

class UpdateTradeOfferPacket(val newOffer: Pair<UUID, PartyPosition>?): NetworkPacket<UpdateTradeOfferPacket> {
    companion object {
        val ID = cobblemonResource("update_trade_offer")
        fun decode(buffer: RegistryFriendlyByteBuf) = UpdateTradeOfferPacket(buffer.readNullable { buffer.readUUID() to buffer.readPartyPosition() })
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeNullable(newOffer) { buffer, (pokemonId, partyPosition) ->
            buffer.writeUUID(pokemonId)
            buffer.writePartyPosition(partyPosition)
        }
    }
}