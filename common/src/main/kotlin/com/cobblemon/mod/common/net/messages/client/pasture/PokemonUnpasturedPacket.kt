/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pasture

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import java.util.UUID
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Packet sent to inform clients a pasture Pokémon was removed, sent to players with the menu open to remove it visibly.
 *
 * @author Hiroku
 * @since April 16th, 2023
 */
class PokemonUnpasturedPacket(val pokemonId: UUID) : NetworkPacket<PokemonUnpasturedPacket> {
    companion object {
        val ID = cobblemonResource("pasture_pokemon_removed")
        fun decode(buffer: RegistryFriendlyByteBuf) = PokemonUnpasturedPacket(buffer.readUUID())
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(pokemonId)
    }
}