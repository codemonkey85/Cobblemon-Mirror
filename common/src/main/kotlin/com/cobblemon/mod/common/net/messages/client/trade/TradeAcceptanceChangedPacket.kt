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
import com.cobblemon.mod.common.util.readUUID
import com.cobblemon.mod.common.util.writeUUID
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

/**
 * Packet sent to the client when the other player has changed the acceptance status for the client's Pokémon.
 *
 * @param pokemonId The UUID of the Pokémon that was offered when the other player changed their acceptance.
 * @param accepted Whether the other player is accepting the offered Pokémon.
 *
 * Handled by [com.cobblemon.mod.common.client.net.trade.TradeAcceptanceChangedHandler]
 *
 * @author Hiroku
 * @since March 5th, 2023
 */
class TradeAcceptanceChangedPacket(val pokemonId: UUID, val accepted: Boolean) : NetworkPacket<TradeAcceptanceChangedPacket> {
    companion object {
        val ID = cobblemonResource("trade_acceptance_changed")
        fun decode(buffer: RegistryFriendlyByteBuf) = TradeAcceptanceChangedPacket(buffer.readUUID(), buffer.readBoolean())
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(pokemonId)
        buffer.writeBoolean(accepted)
    }
}