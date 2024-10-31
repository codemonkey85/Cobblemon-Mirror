/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.storage.party

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.net.UnsplittablePacket
import com.cobblemon.mod.common.api.storage.party.PartyPosition
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readPartyPosition
import com.cobblemon.mod.common.util.writePartyPosition
import java.util.UUID
import net.minecraft.core.RegistryAccess
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Adds the given Pokémon to a specific location in the client storage. This should be a new
 * Pokémon that the client doesn't know about yet.
 *
 * Handled by [com.cobblemon.mod.common.client.net.storage.party.SetPartyPokemonHandler]
 *
 * @author Hiroku
 * @since November 29th, 2021
*/
class SetPartyPokemonPacket internal constructor(val storeID: UUID, val storePosition: PartyPosition, val pokemon: (RegistryAccess) -> Pokemon) : NetworkPacket<SetPartyPokemonPacket>, UnsplittablePacket {

    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(this.storeID)
        buffer.writePartyPosition(this.storePosition)
        Pokemon.S2C_CODEC.encode(buffer, this.pokemon(buffer.registryAccess()))
    }

    companion object {
        val ID = cobblemonResource("set_party_pokemon")
        fun decode(buffer: RegistryFriendlyByteBuf): SetPartyPokemonPacket {
            val uuid = buffer.readUUID()
            val position = buffer.readPartyPosition()
            val bufferCache = buffer.readBytes(buffer.readableBytes())

            return SetPartyPokemonPacket(
                storeID = uuid,
                storePosition = position
            ) { Pokemon.S2C_CODEC.decode(RegistryFriendlyByteBuf(bufferCache, it)) }
        }
    }

}