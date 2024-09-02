/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Packet fired to notify the client that the tradeable status of the Pokémon has been updated.
 *
 * @author Hiroku
 * @since October 7th, 2023
 */
class TradeableUpdatePacket(pokemon: () -> Pokemon, value: Boolean) : SingleUpdatePacket<Boolean, TradeableUpdatePacket>(pokemon, value) {
    override val id = ID
    override fun encodeValue(buffer: RegistryFriendlyByteBuf) {
        buffer.writeBoolean(value)
    }

    override fun set(pokemon: Pokemon, value: Boolean) {
        pokemon.tradeable = value
    }

    companion object {
        val ID = cobblemonResource("tradeable_update")
        fun decode(buffer: RegistryFriendlyByteBuf) = TradeableUpdatePacket(decodePokemon(buffer), buffer.readBoolean())
    }
}