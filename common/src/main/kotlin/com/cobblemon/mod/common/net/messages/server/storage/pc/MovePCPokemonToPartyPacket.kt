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
import com.cobblemon.mod.common.net.serverhandling.storage.pc.MovePCPokemonToPartyHandler
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readPCPosition
import com.cobblemon.mod.common.util.readPartyPosition
import com.cobblemon.mod.common.util.writePCPosition
import com.cobblemon.mod.common.util.writePartyPosition
import java.util.UUID
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Tells the server to move a Pokémon from a player's linked PC to their party. If the party position is
 * not specified, it will attempt to put the Pokémon in the first available space.
 *
 * Handled by [MovePCPokemonToPartyHandler].
 *
 * @author Hiroku
 * @since June 20th, 2022
 */
class MovePCPokemonToPartyPacket(val pokemonID: UUID, val pcPosition: PCPosition, val partyPosition: PartyPosition?) : NetworkPacket<MovePCPokemonToPartyPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(pokemonID)
        buffer.writePCPosition(pcPosition)
        buffer.writeNullable(partyPosition) { pb, value -> pb.writePartyPosition(value) }
    }
    companion object {
        val ID = cobblemonResource("move_pc_pokemon_to_party")
        fun decode(buffer: RegistryFriendlyByteBuf) = MovePCPokemonToPartyPacket(buffer.readUUID(), buffer.readPCPosition(), buffer.readNullable { it.readPartyPosition() })
    }
}