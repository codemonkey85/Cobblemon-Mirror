/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Updates whether the Pokémon has the Gigantamax factor.
 *
 * @author Segfault Guy
 * @since July 27, 2023
 */
class GmaxFactorUpdatePacket(pokemon: () -> Pokemon, value: Boolean) : BooleanUpdatePacket<GmaxFactorUpdatePacket>(pokemon, value) {
    override val id = ID

    override fun set(pokemon: Pokemon, value: Boolean) {
        pokemon.gmaxFactor = value
    }

    companion object {
        val ID = cobblemonResource("gmax_factor_update")
        fun decode(buffer: RegistryFriendlyByteBuf) = GmaxFactorUpdatePacket(decodePokemon(buffer), buffer.readBoolean())
    }
}