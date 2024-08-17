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
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Packet fired when a Pokémon is added to a pasture block and a player has the menu open. This is so GUI updates are
 * applied.
 *
 * @author Hiroku
 * @since April 16th, 2023
 */
class PokemonPasturedPacket(val pasturePokemonDTO: OpenPasturePacket.PasturePokemonDataDTO) : NetworkPacket<PokemonPasturedPacket> {
    companion object {
        val ID = cobblemonResource("pasture_pokemon_added")
        fun decode(buffer: RegistryFriendlyByteBuf) = PokemonPasturedPacket(OpenPasturePacket.PasturePokemonDataDTO.decode(buffer))
    }

    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        pasturePokemonDTO.encode(buffer)
    }
}