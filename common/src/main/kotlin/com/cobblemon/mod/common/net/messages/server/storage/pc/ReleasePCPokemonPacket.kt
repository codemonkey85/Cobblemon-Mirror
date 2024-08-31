/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.storage.pc

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.storage.pc.PCPosition
import com.cobblemon.mod.common.net.serverhandling.storage.party.ReleasePCPokemonHandler
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readPCPosition
import com.cobblemon.mod.common.util.writePCPosition
import java.util.UUID
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Packet sent when the player is releasing one of their Pokémon from their PC.
 *
 * Handled by [ReleasePCPokemonHandler].
 *
 * @author Hiroku
 * @since October 31st, 2022
 */
class ReleasePCPokemonPacket(val pokemonID: UUID, val position: PCPosition) : NetworkPacket<ReleasePCPokemonPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.
        writeUUID(pokemonID)
        buffer.writePCPosition(position)
    }
    companion object {
        val ID = cobblemonResource("release_pc_pokemon")
        fun decode(buffer: RegistryFriendlyByteBuf) = ReleasePCPokemonPacket(buffer.readUUID(), buffer.readPCPosition())
    }
}