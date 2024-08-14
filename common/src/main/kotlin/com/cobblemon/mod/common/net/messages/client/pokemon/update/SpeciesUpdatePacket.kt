/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.registry.CobblemonRegistries
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

class SpeciesUpdatePacket(pokemon: () -> Pokemon, value: Species) : SingleUpdatePacket<Species, SpeciesUpdatePacket>(pokemon, value) {
    override val id = ID
    override fun encodeValue(buffer: RegistryFriendlyByteBuf) {
        buffer.writeResourceKey(value.resourceKey())
    }

    override fun set(pokemon: Pokemon, value: Species) {
        pokemon.species = value
    }

    companion object {
        val ID = cobblemonResource("species_update")
        fun decode(buffer: RegistryFriendlyByteBuf): SpeciesUpdatePacket {
            val pokemon = decodePokemon(buffer)
            val species = buffer.registryAccess()
                .registryOrThrow(CobblemonRegistries.SPECIES_KEY)
                .getOrThrow(buffer.readResourceKey(CobblemonRegistries.SPECIES_KEY))
            return SpeciesUpdatePacket(pokemon, species)
        }
    }

}