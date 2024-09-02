/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.net.messages.client.PokemonUpdatePacket
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Base class for packets which update a single value of a Pokémon.
 *
 * Handled by [com.cobblemon.mod.common.client.net.pokemon.update.PokemonUpdatePacketHandler]
 *
 * @author Hiroku
 * @since November 28th, 2021
 */
abstract class SingleUpdatePacket<T, N : NetworkPacket<N>>(pokemon: () -> Pokemon, val value: T) : PokemonUpdatePacket<N>(pokemon) {

    override fun encodeDetails(buffer: RegistryFriendlyByteBuf) {
        this.encodeValue(buffer)
    }

    override fun applyToPokemon() {
        set(this.pokemon(), this.value)
    }

    abstract fun encodeValue(buffer: RegistryFriendlyByteBuf)

    /** Sets the value in the client-side Pokémon. */
    abstract fun set(pokemon: Pokemon, value: T)
}