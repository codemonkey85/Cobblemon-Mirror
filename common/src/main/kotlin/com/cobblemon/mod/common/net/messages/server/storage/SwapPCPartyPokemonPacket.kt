/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.storage

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.storage.party.PartyPosition
import com.cobblemon.mod.common.api.storage.pc.PCPosition
import com.cobblemon.mod.common.net.serverhandling.storage.SwapPCPartyPokemonHandler
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readPCPosition
import com.cobblemon.mod.common.util.readPartyPosition
import com.cobblemon.mod.common.util.readUUID
import com.cobblemon.mod.common.util.writePCPosition
import com.cobblemon.mod.common.util.writePartyPosition
import com.cobblemon.mod.common.util.writeUUID
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

/**
 * Tells the server to swap Pokémon between the party and the currently linked PC. The positions are sent
 * along with the IDs to validate that the client is making a synchronized request.
 *
 * Handled by [SwapPCPartyPokemonHandler].
 *
 * @author Hiroku
 * @since June 20th, 2022
 */
class SwapPCPartyPokemonPacket(val partyPokemonID: UUID, val partyPosition: PartyPosition, val pcPokemonID: UUID, val pcPosition: PCPosition) : NetworkPacket<SwapPCPartyPokemonPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(partyPokemonID)
        buffer.writePartyPosition(partyPosition)
        buffer.writeUUID(pcPokemonID)
        buffer.writePCPosition(pcPosition)
    }

    companion object {
        val ID = cobblemonResource("swap_pc_party_pokemon")
        fun decode(buffer: RegistryFriendlyByteBuf): SwapPCPartyPokemonPacket = SwapPCPartyPokemonPacket(buffer.readUUID(), buffer.readPartyPosition(), buffer.readUUID(), buffer.readPCPosition())
    }
}