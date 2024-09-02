/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.storage.party

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.storage.party.PartyPosition
import com.cobblemon.mod.common.net.serverhandling.storage.party.MovePartyPokemonHandler
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readPartyPosition
import com.cobblemon.mod.common.util.writePartyPosition
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

/**
 * Tells the server to move a party Pokémon from one position of the player's party to another.
 *
 * Handled by [MovePartyPokemonHandler].
 *
 * @author Hiroku
 * @since June 20th, 2022
 */
class MovePartyPokemonPacket(val pokemonID: UUID, val oldPosition: PartyPosition, val newPosition: PartyPosition) : NetworkPacket<MovePartyPokemonPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(pokemonID)
        buffer.writePartyPosition(oldPosition)
        buffer.writePartyPosition(newPosition)
    }
    companion object {
        val ID = cobblemonResource("move_party_pokemon")
        fun decode(buffer: RegistryFriendlyByteBuf) = MovePartyPokemonPacket(buffer.readUUID(), buffer.readPartyPosition(), buffer.readPartyPosition())
    }
}