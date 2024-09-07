/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.pokemon.Pokemon
import java.util.UUID
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Base packet for all the single-field Pokémon update packets.
 *
 * @author Hiroku
 * @since November 28th, 2021
 */
abstract class PokemonUpdatePacket<T>(val pokemon: () -> Pokemon) : NetworkPacket<T> where T : NetworkPacket<T> {

    final override fun encode(buffer: RegistryFriendlyByteBuf) {
        val pokemon = pokemon()
        // This won't ever happen in instances where packets get sent out, but they protect us from NPEs on fields that require synchronization on load/save
        buffer.writeUUID(pokemon.storeCoordinates.get()?.store?.uuid ?: UUID.randomUUID())
        buffer.writeUUID(pokemon.uuid)
        encodeDetails(buffer)
    }

    abstract fun encodeDetails(buffer: RegistryFriendlyByteBuf)

    /** Applies the update to the located Pokémon. */
    abstract fun applyToPokemon()

    companion object {

        /**
         * Reads the current Pokémon from the given [buffer].
         *
         * @param buffer The [ByteBuf] being decoded.
         * @return The [Pokemon] found.
         */
        fun decodePokemon(buffer: RegistryFriendlyByteBuf) : () -> Pokemon {
            val storeId = buffer.readUUID()
            val pokemonId = buffer.readUUID()
            return { CobblemonClient.storage.locatePokemon(storeId, pokemonId)!! }
        }
    }
}