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
import com.cobblemon.mod.common.net.serverhandling.storage.pc.MovePCPokemonHandler
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readPCPosition
import com.cobblemon.mod.common.util.readUUID
import com.cobblemon.mod.common.util.writePCPosition
import com.cobblemon.mod.common.util.writeUUID
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

/**
 * Tells the server to move a PC Pokémon from one position of the player's currently linked PC to another.
 *
 * Handled by [MovePCPokemonHandler].
 *
 * @author Hiroku
 * @since June 20th, 2022
 */
class MovePCPokemonPacket(val pokemonID: UUID, val oldPosition: PCPosition, val newPosition: PCPosition) : NetworkPacket<MovePCPokemonPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(pokemonID)
        buffer.writePCPosition(oldPosition)
        buffer.writePCPosition(newPosition)
    }
    companion object {
        val ID = cobblemonResource("move_pc_pokemon")
        fun decode(buffer: RegistryFriendlyByteBuf) = MovePCPokemonPacket(buffer.readUUID(), buffer.readPCPosition(), buffer.readPCPosition())
    }
}