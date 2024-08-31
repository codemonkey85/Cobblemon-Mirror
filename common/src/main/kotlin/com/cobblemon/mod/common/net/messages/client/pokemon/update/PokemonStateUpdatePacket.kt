/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.activestate.PokemonState
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

class PokemonStateUpdatePacket(pokemon: () -> Pokemon, value: PokemonState): SingleUpdatePacket<PokemonState, PokemonStateUpdatePacket>(pokemon, value) {

    override val id = ID

    override fun encodeValue(buffer: RegistryFriendlyByteBuf) {
        value.writeToBuffer(buffer)
    }

    override fun set(pokemon: Pokemon, value: PokemonState) { pokemon.state = value }

    companion object {
        val ID = cobblemonResource("state_update")
        fun decode(buffer: RegistryFriendlyByteBuf): PokemonStateUpdatePacket {
            val pokemon = decodePokemon(buffer)
            val state = PokemonState.fromBuffer(buffer)
            return PokemonStateUpdatePacket(pokemon, state)
        }
    }

}