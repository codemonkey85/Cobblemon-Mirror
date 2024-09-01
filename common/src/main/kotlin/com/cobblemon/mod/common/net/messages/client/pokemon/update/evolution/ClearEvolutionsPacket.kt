/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update.evolution

import com.cobblemon.mod.common.net.messages.client.PokemonUpdatePacket
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

class ClearEvolutionsPacket(pokemon: () -> Pokemon) : PokemonUpdatePacket<ClearEvolutionsPacket>(pokemon) {

    override val id = ID

    override fun encodeDetails(buffer: RegistryFriendlyByteBuf) {}

    override fun applyToPokemon() {
        this.pokemon().evolutionProxy.client().clear()
    }

    companion object {
        val ID = cobblemonResource("clear_evolutions")
        fun decode(buffer: RegistryFriendlyByteBuf) = ClearEvolutionsPacket(decodePokemon(buffer))
    }

}