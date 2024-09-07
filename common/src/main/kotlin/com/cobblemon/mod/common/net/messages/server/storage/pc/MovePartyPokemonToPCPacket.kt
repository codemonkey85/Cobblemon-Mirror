/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.storage.pc

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.storage.party.PartyPosition
import com.cobblemon.mod.common.api.storage.pc.PCPosition
import com.cobblemon.mod.common.net.serverhandling.storage.pc.MovePartyPokemonToPCHandler
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readNullable
import com.cobblemon.mod.common.util.readPCPosition
import com.cobblemon.mod.common.util.readPartyPosition
import com.cobblemon.mod.common.util.readUUID
import com.cobblemon.mod.common.util.writeNullable
import com.cobblemon.mod.common.util.writePCPosition
import com.cobblemon.mod.common.util.writePartyPosition
import com.cobblemon.mod.common.util.writeUUID
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

/**
 * Tells the server to move a Pokémon from a player's party to their linked PC. If the PC position is
 * not specified, it will attempt to put the Pokémon in the first available space.
 *
 * Handled by [MovePartyPokemonToPCHandler].
 *
 * @author Hiroku
 * @since June 20th, 2022
 */
class MovePartyPokemonToPCPacket(val pokemonID: UUID, val partyPosition: PartyPosition, val pcPosition: PCPosition?) : NetworkPacket<MovePartyPokemonToPCPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(pokemonID)
        buffer.writePartyPosition(partyPosition)
        buffer.writeNullable(pcPosition) { pb, value -> pb.writePCPosition(value) }
    }
    companion object {
        val ID = cobblemonResource("move_party_pokemon_to_pc")
        fun decode(buffer: RegistryFriendlyByteBuf) = MovePartyPokemonToPCPacket(buffer.readUUID(), buffer.readPartyPosition(), buffer.readNullable { it.readPCPosition() })
    }
}