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
import com.cobblemon.mod.common.net.serverhandling.storage.pc.ReleasePartyPokemonHandler
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readPartyPosition
import com.cobblemon.mod.common.util.writePartyPosition
import java.util.UUID
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Packet sent when the player is releasing one of their Pokémon from their party.
 *
 * Handled by [ReleasePartyPokemonHandler]
 *
 * @author Hiroku
 * @since October 31st, 2022
 */
class ReleasePartyPokemonPacket(val pokemonID: UUID, val position: PartyPosition) : NetworkPacket<ReleasePartyPokemonPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(pokemonID)
        buffer.writePartyPosition(position)
    }
    companion object {
        val ID = cobblemonResource("release_party_pokemon")
        fun decode(buffer: RegistryFriendlyByteBuf) = ReleasePartyPokemonPacket(buffer.readUUID(), buffer.readPartyPosition())
    }
}