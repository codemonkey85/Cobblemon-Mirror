/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.pokemon.Natures
import com.cobblemon.mod.common.net.messages.client.PokemonUpdatePacket
import com.cobblemon.mod.common.pokemon.Nature
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.writeIdentifier
import com.cobblemon.mod.common.util.writeNullable
import net.minecraft.network.RegistryFriendlyByteBuf

class NatureUpdatePacket(pokemon: () -> Pokemon, val nature: Nature?, val minted: Boolean) : PokemonUpdatePacket<NatureUpdatePacket>(pokemon) {

    override val id = ID

    override fun encodeDetails(buffer: RegistryFriendlyByteBuf) {
        buffer.writeNullable(nature) { _, v -> buffer.writeIdentifier(v.name) }
        buffer.writeBoolean(this.minted)
    }

    override fun applyToPokemon() {
        // Check for removing mint
        if (minted && nature == null) {
            pokemon().mintedNature = null
            return
        } else {
            // Validate the nature locally
            if (nature == null) {
                LOGGER.warn("A null nature was attempted to be put onto: '$pokemon'")
                return
            }

            // Check which nature to modify
            if (!minted) {
                pokemon().nature = nature
            } else {
                pokemon().mintedNature = nature
            }
        }
    }

    companion object {
        val ID = cobblemonResource("nature_update")
        fun decode(buffer: RegistryFriendlyByteBuf) = NatureUpdatePacket(decodePokemon(buffer), buffer.readNullable { Natures.getNature(buffer.readIdentifier()) }, buffer.readBoolean())
    }

}