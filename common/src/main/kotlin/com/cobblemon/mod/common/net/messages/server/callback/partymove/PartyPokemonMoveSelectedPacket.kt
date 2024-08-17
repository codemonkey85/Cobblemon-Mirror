/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.callback.partymove

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.writeSizedInt
import java.util.UUID
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Packet sent to the server when the player has responded to a party move selection callback.
 *
 * @author Hiroku
 * @since July 29th, 2023
 */
class PartyPokemonMoveSelectedPacket(val uuid: UUID, val pokemonIndex: Int, val moveIndex: Int) : NetworkPacket<PartyPokemonMoveSelectedPacket> {
    companion object {
        val ID = cobblemonResource("party_pokemon_move_selected")
        fun decode(buffer: RegistryFriendlyByteBuf) = PartyPokemonMoveSelectedPacket(buffer.readUUID(), buffer.readSizedInt(IntSize.U_BYTE), buffer.readSizedInt(IntSize.U_BYTE))
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(uuid)
        buffer.writeSizedInt(IntSize.U_BYTE, pokemonIndex)
        buffer.writeSizedInt(IntSize.U_BYTE, moveIndex)
    }
}