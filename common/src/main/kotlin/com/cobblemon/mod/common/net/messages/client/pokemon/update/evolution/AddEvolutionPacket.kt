/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update.evolution

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionDisplayEvent
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.evolution.Evolution
import com.cobblemon.mod.common.api.pokemon.evolution.EvolutionDisplay
import com.cobblemon.mod.common.net.messages.client.pokemon.update.SingleUpdatePacket
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.evolution.CobblemonEvolutionDisplay
import com.cobblemon.mod.common.util.*
import io.netty.buffer.ByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf

class AddEvolutionPacket(pokemon: () -> Pokemon, value: EvolutionDisplay) : SingleUpdatePacket<EvolutionDisplay, AddEvolutionPacket>(pokemon, value) {

    override val id = ID

    constructor(pokemon: Pokemon, value: Evolution) : this({ pokemon }, value.convertToDisplay(pokemon))

    override fun encodeValue(buffer: RegistryFriendlyByteBuf) {
        this.value.encode(buffer)
    }

    override fun set(pokemon: Pokemon, value: EvolutionDisplay) {
        pokemon.evolutionProxy.client().add(value)
    }

    companion object {

        val ID = cobblemonResource("add_evolution")

        fun decode(buffer: RegistryFriendlyByteBuf) = AddEvolutionPacket(decodePokemon(buffer), decodeDisplay(buffer))

        internal fun Evolution.convertToDisplay(pokemon: Pokemon): EvolutionDisplay {
            val result = pokemon.clone()
            this.result.apply(result)
            val expectedDisplay = CobblemonEvolutionDisplay(this.id, result)
            val event = EvolutionDisplayEvent(result, expectedDisplay, this)
            CobblemonEvents.EVOLUTION_DISPLAY.post(event)
            return event.display
        }

        internal fun EvolutionDisplay.encode(buffer: RegistryFriendlyByteBuf) {
            buffer.writeString(this.id)
            buffer.writeIdentifier(this.species.resourceIdentifier)
            buffer.writeCollection(this.aspects) { pb, value -> pb.writeString(value) }
        }

        internal fun decodeDisplay(buffer: RegistryFriendlyByteBuf): EvolutionDisplay {
            val id = buffer.readString()
            val speciesIdentifier = buffer.readIdentifier()
            val species = PokemonSpecies.getByIdentifier(speciesIdentifier)
                ?: throw IllegalArgumentException("Cannot resolve species from $speciesIdentifier")
            val aspects = buffer.readList(ByteBuf::readString).toSet()
            return CobblemonEvolutionDisplay(id, species, aspects)
        }
    }
}