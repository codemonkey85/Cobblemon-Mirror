/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.pasture

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readUUID
import com.cobblemon.mod.common.util.writeUUID
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

/**
 * Packet sent to the server to indicate that a pastured Pokémon should be removed.
 *
 * @author Hiroku
 * @since April 16th, 2023
 */
class UnpasturePokemonPacket(val pastureId: UUID, val pokemonId: UUID) : NetworkPacket<UnpasturePokemonPacket> {
    companion object {
        val ID = cobblemonResource("unpasture_pokemon")
        fun decode(buffer: RegistryFriendlyByteBuf) = UnpasturePokemonPacket(buffer.readUUID(), buffer.readUUID())
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(pastureId)
        buffer.writeUUID(pokemonId)
    }
}