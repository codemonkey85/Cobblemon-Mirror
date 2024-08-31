/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.storage.party

import com.cobblemon.mod.common.api.storage.party.PartyPosition
import com.cobblemon.mod.common.net.messages.client.storage.MoveClientPokemonPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readPartyPosition
import com.cobblemon.mod.common.util.readUUID
import com.cobblemon.mod.common.util.writePartyPosition
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

/**
 * Moves a Pokémon from one party place to another on the client side.
 *
 * Handled by [com.cobblemon.mod.common.client.net.storage.party.MoveClientPartyPokemonHandler]
 *
 * @author Hiroku
 * @since November 29th, 2021
 */
class MoveClientPartyPokemonPacket(storeID: UUID, pokemonID: UUID, newPosition: PartyPosition) : MoveClientPokemonPacket<PartyPosition, MoveClientPartyPokemonPacket>(storeID, pokemonID, newPosition) {
    override val id = ID
    override fun encodePosition(buffer: RegistryFriendlyByteBuf, position: PartyPosition) = buffer.writePartyPosition(newPosition)
    companion object {
        val ID = cobblemonResource("move_client_party_pokemon")
        fun decode(buffer: RegistryFriendlyByteBuf) = MoveClientPartyPokemonPacket(buffer.readUUID(), buffer.readUUID(), buffer.readPartyPosition())
    }
}